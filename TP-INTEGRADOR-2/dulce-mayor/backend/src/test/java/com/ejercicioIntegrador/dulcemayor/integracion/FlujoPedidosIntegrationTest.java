package com.ejercicioIntegrador.dulcemayor.integracion;

import com.ejercicioIntegrador.dulcemayor.dto.CategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCategoria;
import com.ejercicioIntegrador.dulcemayor.service.ServicioImagen;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProducto;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSubCategoria;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVigenciaPrecio;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo de punta a punta de la Ola 2 con la cadena de Spring Security real y H2:
 * alta de cliente y punto de venta por el preventista -> ruta armada por administración -> ruta ordenada desde
 * la ubicación -> pedido (descuenta stock y marca la visita) -> el cliente confirma -> asignación al distribuidor
 * -> entrega no concretada (sigue pendiente) -> otro cliente anula (devuelve stock). Incluye permisos por rol.
 * Los servicios externos (Nominatim, OSRM) no se consultan: sus URL de prueba no responden y el trayecto
 * se informa como línea recta.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlujoPedidosIntegrationTest {

    private static final String JEFE = "jefe@dulcemayor.test";
    private static final String CLAVE = "Segura2026x";

    @Autowired private WebApplicationContext contexto;
    @Autowired private ServicioCategoria servicioCategoria;
    @Autowired private ServicioSubCategoria servicioSubCategoria;
    @Autowired private ServicioImagen servicioImagen;
    @Autowired private ServicioProducto servicioProducto;
    @Autowired private ServicioVigenciaPrecio servicioVigenciaPrecio;
    @Autowired private ServicioStock servicioStock;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    // ------------------------------------------------------------------ auxiliares

    private String login(String correo, String clave) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(cuerpo, "$.token");
    }

    /** Ingresa con la clave temporal y la cambia: devuelve el token ya habilitado. */
    private String habilitar(String correo) throws Exception {
        String cuerpo = mvc.perform(put("/api/auth/clave").header("Authorization", login(correo, "dulcemayor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claveActual\":\"dulcemayor\",\"nuevaClave\":\"" + CLAVE + "\",\"confirmarClave\":\"" + CLAVE + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(cuerpo, "$.token");
    }

    private String enviar(String metodo, String url, String token, String json, int esperado) throws Exception {
        var pedido = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            default -> get(url);
        };
        if (token != null) {
            pedido = pedido.header("Authorization", token);
        }
        if (json != null) {
            pedido = pedido.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mvc.perform(pedido).andExpect(status().is(esperado)).andReturn().getResponse().getContentAsString();
    }

    private String idCiudadDeMendoza(String token) throws Exception {
        String idPais = JsonPath.read(enviar("GET", "/api/paises", token, null, 200), "$[0].id");
        String idProvincia = JsonPath.read(enviar("GET", "/api/provincias/pais/" + idPais, token, null, 200), "$[0].id");
        List<String> capital = JsonPath.read(enviar("GET", "/api/departamentos/provincia/" + idProvincia, token, null, 200),
                "$[?(@.nombre == 'Capital')].id");
        return JsonPath.read(enviar("GET", "/api/localidades/departamento/" + capital.get(0), token, null, 200), "$[0].id");
    }

    private String empleado(String jefe, String rol, String correo, String dni, String idLocalidad) throws Exception {
        String json = """
                {"nombre":"Juan","apellido":"Pérez","fechaNacimiento":"1990-05-20","tipoDocumento":"DNI",
                 "numeroDocumento":"%s","correo":"%s","rol":"%s","vehiculo":"Kangoo","patente":"AB123CD",
                 "direccion":{"calle":"Colón","numeracion":"100","idLocalidad":"%s"}}
                """.formatted(dni, correo, rol, idLocalidad);
        return JsonPath.read(enviar("POST", "/api/empleados", jefe, json, 201), "$.id");
    }

    /** Producto con precio vigente y stock, cargado con los servicios del módulo de catálogo. */
    private String producto(String codigo, double precio, int stock) throws Exception {
        String idCategoria = servicioCategoria.listarActivos().isEmpty()
                ? servicioCategoria.alta(new CategoriaRequestDTO("Golosinas")).id()
                : servicioCategoria.listarActivos().get(0).id();
        String idSub = servicioSubCategoria.listarActivos().isEmpty()
                ? servicioSubCategoria.alta(new SubCategoriaRequestDTO("Alfajores", idCategoria)).id()
                : servicioSubCategoria.listarActivos().get(0).id();
        String idImagen = servicioImagen.alta(new ImagenRequestDTO(codigo + ".png", "image/png", new byte[]{1, 2},
                TipoImagen.PRODUCTO)).id();
        String id = servicioProducto.alta(new ProductoRequestDTO(codigo, "Producto " + codigo, "", Presentacion.CAJA, 24,
                false, idSub, List.of(idImagen))).id();
        servicioVigenciaPrecio.alta(new VigenciaPrecioRequestDTO(LocalDate.now(), null, precio, id));
        servicioStock.alta(new StockRequestDTO(id, stock, 1));
        return id;
    }

    // ------------------------------------------------------------------ pruebas

    @Test
    void flujoCompletoDelPreventistaAlDistribuidor() throws Exception {
        String jefe = habilitar(JEFE);
        String ciudad = idCiudadDeMendoza(jefe);
        String idPreventista = empleado(jefe, "PREVENTISTA", "prev@dm.test", "30111222", ciudad);
        String idDistribuidor = empleado(jefe, "DISTRIBUIDOR", "dist@dm.test", "30111333", ciudad);
        String preventista = habilitar("prev@dm.test");
        String distribuidor = habilitar("dist@dm.test");
        String alfajor = producto("ALF-1", 1000, 10);

        // Alta de cliente en el celular del preventista: queda con la clave temporal y debe cambiarla
        String idCliente = JsonPath.read(enviar("POST", "/api/clientes/campo", preventista, """
                {"nombre":"Ana","apellido":"López","correo":"ana@kiosco.test","tipoDocumento":"DNI","numeroDocumento":"28111222","fechaNacimiento":"1985-03-10"}
                """, 201), "$.id");
        // Punto de venta (HU-17) con coordenadas; sin coordenadas se rechaza
        enviar("POST", "/api/puntos-venta", preventista, """
                {"nombreFantasia":"Kiosco El Sol","razonSocial":"Ana López","cuit":"20123456786","condicionIva":"MONOTRIBUTISTA",
                 "idCliente":"%s","direccion":{"calle":"San Martín","numeracion":"1245","idLocalidad":"%s"}}
                """.formatted(idCliente, ciudad), 400);
        String idPv = JsonPath.read(enviar("POST", "/api/puntos-venta", preventista, """
                {"nombreFantasia":"Kiosco El Sol","razonSocial":"Ana López","cuit":"20-12345678-6","condicionIva":"MONOTRIBUTISTA",
                 "idCliente":"%s","direccion":{"calle":"San Martín","numeracion":"1245","latitud":-32.8895,"longitud":-68.8458,"idLocalidad":"%s"}}
                """.formatted(idCliente, ciudad), 201), "$.id");

        // Ruta armada por administración (el jefe hereda ADMINISTRATIVO); el preventista no puede armarla
        String rutaJson = "{\"fecha\":\"%s\",\"tipoRuta\":\"VISITA\",\"idEmpleado\":\"%s\",\"idsPuntoVenta\":[\"%s\"]}"
                .formatted(LocalDate.now(), idPreventista, idPv);
        enviar("POST", "/api/rutas", preventista, rutaJson, 403);
        enviar("POST", "/api/rutas", jefe, rutaJson, 201);

        // El preventista ordena su ruta desde su ubicación
        String ruta = enviar("POST", "/api/rutas/mia/ordenar", preventista, "{\"latitud\":-32.89,\"longitud\":-68.84}", 200);
        assertEquals(idPv, JsonPath.read(ruta, "$.siguiente.idPuntoVenta"));

        // HU-21: pedido; no se puede pedir más que el stock
        enviar("POST", "/api/pedidos/preventista", preventista,
                "{\"idPuntoVenta\":\"%s\",\"detalles\":[{\"idProducto\":\"%s\",\"cantidad\":11}]}".formatted(idPv, alfajor), 400);
        String pedido = enviar("POST", "/api/pedidos/preventista", preventista,
                "{\"idPuntoVenta\":\"%s\",\"detalles\":[{\"idProducto\":\"%s\",\"cantidad\":3}]}".formatted(idPv, alfajor), 201);
        String idPedido = JsonPath.read(pedido, "$.id");
        assertEquals("CARGADO", JsonPath.read(pedido, "$.estado"));
        assertEquals(7, servicioStock.cantidadDisponible(alfajor));
        mvc.perform(get("/api/rutas/mia").header("Authorization", preventista))
                .andExpect(jsonPath("$.ruta.puntos[0].resultado").value("CON_PEDIDO"));

        // El cliente ve y confirma SU pedido (por POST); otro cliente no lo ve
        String cliente = habilitar("ana@kiosco.test");
        enviar("POST", "/api/auth/registro", null, """
                {"nombre":"Beto","apellido":"Sosa","correo":"beto@almacen.test","clave":"Almacen2026","confirmacionClave":"Almacen2026",
                 "tipoDocumento":"DNI","numeroDocumento":"29333444","fechaNacimiento":"1985-03-10"}
                """, 201);
        String otroCliente = login("beto@almacen.test", "Almacen2026"); // registro institucional: elige su clave
        enviar("GET", "/api/pedidos/mis-pedidos/" + idPedido, otroCliente, null, 404);
        enviar("GET", "/api/pedidos/mis-pedidos/" + idPedido + "/confirmar", cliente, null, 405);
        enviar("POST", "/api/pedidos/mis-pedidos/" + idPedido + "/confirmar", preventista, null, 403);
        mvc.perform(post("/api/pedidos/mis-pedidos/" + idPedido + "/confirmar").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_DE_ENTREGA"))
                .andExpect(jsonPath("$.confirmado").value(true));

        // Asignación al distribuidor y ruta de entregas
        enviar("POST", "/api/pedidos/asignacion", distribuidor,
                "{\"idDistribuidor\":\"%s\",\"idsPedido\":[\"%s\"]}".formatted(idDistribuidor, idPedido), 403);
        enviar("POST", "/api/pedidos/asignacion", jefe,
                "{\"idDistribuidor\":\"%s\",\"idsPedido\":[\"%s\"]}".formatted(idDistribuidor, idPedido), 200);
        mvc.perform(post("/api/pedidos/entregas/ruta").header("Authorization", distribuidor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruta.tipoRuta").value("ENTREGA"))
                .andExpect(jsonPath("$.ruta.puntos[0].pedidos[0].total").value(3630.0)); // total facturado: 3000 + IVA 21 %

        // Entrega no concretada: sigue pendiente; el stock no cambia
        enviar("POST", "/api/pedidos/entregas/" + idPv + "/no-entregado", distribuidor, "{\"motivo\":\"Local cerrado\"}", 204);
        mvc.perform(get("/api/pedidos/entregas/pendientes").header("Authorization", distribuidor))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE_DE_ENTREGA"));
        assertEquals(7, servicioStock.cantidadDisponible(alfajor));
        // Un preventista no usa los endpoints del distribuidor
        enviar("GET", "/api/pedidos/entregas/pendientes", preventista, null, 403);
    }

    @Test
    void elClienteAnulaYElStockVuelveUnaSolaVez() throws Exception {
        String jefe = habilitar(JEFE);
        String ciudad = idCiudadDeMendoza(jefe);
        empleado(jefe, "PREVENTISTA", "prev@dm.test", "30111222", ciudad);
        String preventista = habilitar("prev@dm.test");
        String alfajor = producto("ALF-2", 500, 5);
        String idCliente = JsonPath.read(enviar("POST", "/api/clientes/campo", preventista, """
                {"nombre":"Ana","apellido":"López","correo":"ana@kiosco.test","tipoDocumento":"DNI","numeroDocumento":"28111222","fechaNacimiento":"1985-03-10"}
                """, 201), "$.id");
        String idPv = JsonPath.read(enviar("POST", "/api/puntos-venta", preventista, """
                {"nombreFantasia":"Kiosco","razonSocial":"Ana López","cuit":"20123456786","condicionIva":"MONOTRIBUTISTA",
                 "idCliente":"%s","direccion":{"calle":"Mitre","numeracion":"1","latitud":-32.88,"longitud":-68.84,"idLocalidad":"%s"}}
                """.formatted(idCliente, ciudad), 201), "$.id");
        String idPedido = JsonPath.read(enviar("POST", "/api/pedidos/preventista", preventista,
                "{\"idPuntoVenta\":\"%s\",\"detalles\":[{\"idProducto\":\"%s\",\"cantidad\":4}]}".formatted(idPv, alfajor), 201), "$.id");
        assertEquals(1, servicioStock.cantidadDisponible(alfajor));

        String cliente = habilitar("ana@kiosco.test");
        enviar("POST", "/api/pedidos/mis-pedidos/" + idPedido + "/anular", cliente, null, 200);
        assertEquals(5, servicioStock.cantidadDisponible(alfajor));
        enviar("POST", "/api/pedidos/mis-pedidos/" + idPedido + "/anular", cliente, null, 400);
        assertEquals(5, servicioStock.cantidadDisponible(alfajor));
    }

    @Test
    void cadaRolSoloAccedeASusEndpoints() throws Exception {
        String jefe = habilitar(JEFE);
        enviar("GET", "/api/rutas/mia", null, null, 401);
        enviar("GET", "/api/pedidos/mis-pedidos", jefe, null, 403);
        enviar("GET", "/api/pedidos/entregas/pendientes", jefe, null, 403);
        enviar("GET", "/api/mapas/geocodificar?direccion=Belgrano 441", null, null, 401);
        enviar("POST", "/api/auth/registro", null, """
                {"nombre":"Beto","apellido":"Sosa","correo":"beto@almacen.test","clave":"Almacen2026","confirmacionClave":"Almacen2026",
                 "tipoDocumento":"DNI","numeroDocumento":"29333444","fechaNacimiento":"1985-03-10"}
                """, 201);
        String cliente = login("beto@almacen.test", "Almacen2026");
        enviar("GET", "/api/pedidos/mis-pedidos", cliente, null, 200);
        enviar("GET", "/api/puntos-venta", cliente, null, 403);
        enviar("POST", "/api/clientes/campo", cliente, "{}", 403);
        enviar("GET", "/api/pedidos", cliente, null, 403);
    }
}
