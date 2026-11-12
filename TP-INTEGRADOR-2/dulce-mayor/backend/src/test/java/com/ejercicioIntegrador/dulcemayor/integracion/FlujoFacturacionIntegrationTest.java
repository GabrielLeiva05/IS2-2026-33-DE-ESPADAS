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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ola 3 (Facturación) y correcciones de las Olas 1 y 2, de punta a punta con Spring Security real y H2:
 *  - al confirmar el pedido se emite la FacturaCliente (subtotal sin IVA, IVA 21 %, total), una sola vez;
 *  - el cliente solo ve y descarga SUS facturas (otro id → 404), el PDF sale de JasperReports con la base real;
 *  - proveedores y facturas de proveedor (número único por proveedor);
 *  - el cliente dado de alta en campo no accede a nada hasta cambiar "dulcemayor";
 *  - el DNI no se cambia desde administración; permisos de los gráficos; renovación de la sesión.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlujoFacturacionIntegrationTest {

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
            case "DELETE" -> delete(url);
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

    /** Cliente en campo + punto de venta + pedido cargado. Devuelve [idCliente, idPuntoVenta, idPedido]. */
    private String[] pedidoDe(String preventista, String ciudad, String correo, String dni, String cuit,
                              String idProducto, int cantidad) throws Exception {
        String idCliente = JsonPath.read(enviar("POST", "/api/clientes/campo", preventista, """
                {"nombre":"Ana","apellido":"López","correo":"%s","tipoDocumento":"DNI","numeroDocumento":"%s","fechaNacimiento":"1985-03-10"}
                """.formatted(correo, dni), 201), "$.id");
        String idPv = JsonPath.read(enviar("POST", "/api/puntos-venta", preventista, """
                {"nombreFantasia":"Kiosco %s","razonSocial":"Ana López","cuit":"%s","condicionIva":"MONOTRIBUTISTA",
                 "idCliente":"%s","direccion":{"calle":"Mitre","numeracion":"10","latitud":-32.88,"longitud":-68.84,"idLocalidad":"%s"}}
                """.formatted(dni, cuit, idCliente, ciudad), 201), "$.id");
        String idPedido = JsonPath.read(enviar("POST", "/api/pedidos/preventista", preventista,
                "{\"idPuntoVenta\":\"%s\",\"detalles\":[{\"idProducto\":\"%s\",\"cantidad\":%d}]}".formatted(idPv, idProducto, cantidad),
                201), "$.id");
        return new String[]{idCliente, idPv, idPedido};
    }

    // ------------------------------------------------------------------ pruebas

    @Test
    void alConfirmarSeFacturaYElClienteSoloAccedeASusFacturas() throws Exception {
        String jefe = habilitar(JEFE);
        String ciudad = idCiudadDeMendoza(jefe);
        empleado(jefe, "PREVENTISTA", "prev@dm.test", "30111222", ciudad);
        String preventista = habilitar("prev@dm.test");
        String alfajor = producto("ALF-1", 1000, 20);

        String[] ana = pedidoDe(preventista, ciudad, "ana@kiosco.test", "28111222", "20123456786", alfajor, 3);
        String[] beto = pedidoDe(preventista, ciudad, "beto@almacen.test", "29333444", "30712345671", alfajor, 1);

        // Alta en campo: con "dulcemayor" no accede a nada hasta cambiarla (lo controla el backend)
        String anaSinCambiar = login("ana@kiosco.test", "dulcemayor");
        enviar("GET", "/api/facturas/mis-facturas", anaSinCambiar, null, 403);
        enviar("POST", "/api/pedidos/mis-pedidos/" + ana[2] + "/confirmar", anaSinCambiar, null, 403);
        String clienteAna = habilitar("ana@kiosco.test");
        String clienteBeto = habilitar("beto@almacen.test");

        // Antes de confirmar no hay factura
        assertEquals(0, (int) JsonPath.read(enviar("GET", "/api/facturas/mis-facturas", clienteAna, null, 200), "$.length()"));

        // Confirmar emite la factura en la misma operación: 3 x 1000 = 3000 + IVA 630 = 3630
        String confirmado = enviar("POST", "/api/pedidos/mis-pedidos/" + ana[2] + "/confirmar", clienteAna, null, 200);
        String idFacturaAna = JsonPath.read(confirmado, "$.idFactura");
        assertEquals(3630.0, ((Number) JsonPath.read(confirmado, "$.totalFacturado")).doubleValue(), 0.001);
        String factura = enviar("GET", "/api/facturas/mis-facturas/" + idFacturaAna, clienteAna, null, 200);
        assertEquals(3000.0, ((Number) JsonPath.read(factura, "$.subtotal")).doubleValue(), 0.001);
        assertEquals(630.0, ((Number) JsonPath.read(factura, "$.iva")).doubleValue(), 0.001);
        assertEquals(3630.0, ((Number) JsonPath.read(factura, "$.total")).doubleValue(), 0.001);
        assertEquals("PENDIENTE_PAGO", JsonPath.read(factura, "$.estado"));
        assertEquals(1, ((Number) JsonPath.read(factura, "$.numeroFactura")).intValue());

        // Confirmar de nuevo no genera otra factura
        enviar("POST", "/api/pedidos/mis-pedidos/" + ana[2] + "/confirmar", clienteAna, null, 400);
        assertEquals(1, (int) JsonPath.read(enviar("GET", "/api/facturas/clientes", jefe, null, 200), "$.length()"));

        // Beto confirma la suya; no puede ver ni descargar la de Ana cambiando el id
        String idFacturaBeto = JsonPath.read(enviar("POST", "/api/pedidos/mis-pedidos/" + beto[2] + "/confirmar",
                clienteBeto, null, 200), "$.idFactura");
        enviar("GET", "/api/facturas/mis-facturas/" + idFacturaAna, clienteBeto, null, 404);
        enviar("GET", "/api/facturas/mis-facturas/" + idFacturaAna + "/pdf", clienteBeto, null, 404);
        assertEquals(1, (int) JsonPath.read(enviar("GET", "/api/facturas/mis-facturas", clienteBeto, null, 200), "$.length()"));
        // Un cliente no usa los endpoints de administración
        enviar("GET", "/api/facturas/clientes/" + idFacturaAna, clienteBeto, null, 403);
        enviar("GET", "/api/facturas/clientes/" + idFacturaAna + "/pdf", clienteBeto, null, 403);
        // Un preventista tampoco ve facturas
        enviar("GET", "/api/facturas/clientes", preventista, null, 403);
        enviar("GET", "/api/facturas/mis-facturas", preventista, null, 403);

        // PDF propio (JasperReports con el DataSource de Spring)
        MvcResult pdf = mvc.perform(get("/api/facturas/mis-facturas/" + idFacturaBeto + "/pdf").header("Authorization", clienteBeto))
                .andExpect(status().isOk()).andReturn();
        assertEquals("application/pdf", pdf.getResponse().getContentType());
        byte[] bytes = pdf.getResponse().getContentAsByteArray();
        assertTrue(bytes.length > 1000);
        assertEquals("%PDF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
        assertTrue(pdf.getResponse().getHeader("Content-Disposition").contains("attachment"));
        // Administración también lo descarga
        mvc.perform(get("/api/facturas/clientes/" + idFacturaAna + "/pdf").header("Authorization", jefe))
                .andExpect(status().isOk());

        // Las facturas no se dan de baja; un pedido confirmado ya no se anula
        enviar("DELETE", "/api/facturas/clientes/" + idFacturaAna, jefe, null, 405);
        enviar("POST", "/api/pedidos/mis-pedidos/" + ana[2] + "/anular", clienteAna, null, 400);

        // Baja de producto: bloqueada mientras esté en pedidos sin entregar
        enviar("DELETE", "/api/productos/" + alfajor, jefe, null, 400);
    }

    @Test
    void proveedoresYFacturasDeProveedor() throws Exception {
        String jefe = habilitar(JEFE);
        String ciudad = idCiudadDeMendoza(jefe);
        String alfajor = producto("ALF-9", 1000, 5);
        String direccion = "{\"calle\":\"Acceso Este\",\"numeracion\":\"3200\",\"idLocalidad\":\"" + ciudad + "\"}";

        enviar("POST", "/api/proveedores", jefe, "{\"razonSocial\":\"Arcor\",\"cuit\":\"30712345670\",\"direccion\":" + direccion + "}", 400);
        String idProveedor = JsonPath.read(enviar("POST", "/api/proveedores", jefe,
                "{\"razonSocial\":\"Arcor SAIC\",\"cuit\":\"30-71234567-1\",\"direccion\":" + direccion + "}", 201), "$.id");
        enviar("POST", "/api/proveedores", jefe,
                "{\"razonSocial\":\"Otro\",\"cuit\":\"30712345671\",\"direccion\":" + direccion + "}", 400);

        String factura = """
                {"idProveedor":"%s","numeroFactura":4521,"fechaFactura":"%s",
                 "detalles":[{"idProducto":"%s","cantidad":10,"precioUnitario":700}]}
                """.formatted(idProveedor, LocalDate.now().minusDays(1), alfajor);
        String creada = enviar("POST", "/api/facturas/proveedores", jefe, factura, 201);
        assertEquals(8470.0, ((Number) JsonPath.read(creada, "$.total")).doubleValue(), 0.001);
        assertEquals("Arcor SAIC", JsonPath.read(creada, "$.razonSocialProveedor"));
        enviar("POST", "/api/facturas/proveedores", jefe, factura, 400); // número repetido para el proveedor
        assertEquals(5, servicioStock.cantidadDisponible(alfajor));      // no mueve stock
        assertEquals(1, (int) JsonPath.read(enviar("GET", "/api/facturas/proveedores/proveedor/" + idProveedor, jefe, null, 200), "$.length()"));

        MvcResult pdf = mvc.perform(get("/api/facturas/proveedores/" + JsonPath.read(creada, "$.id") + "/pdf")
                .header("Authorization", jefe)).andExpect(status().isOk()).andReturn();
        assertEquals("%PDF", new String(pdf.getResponse().getContentAsByteArray(), 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void permisosDeGraficosDniYRenovacionDeSesion() throws Exception {
        String jefe = habilitar(JEFE);
        String ciudad = idCiudadDeMendoza(jefe);
        String idAdministrativo = empleado(jefe, "ADMINISTRATIVO", "adm@dm.test", "30111555", ciudad);
        String administrativo = habilitar("adm@dm.test");
        producto("ALF-5", 100, 7);

        // Gráficos (HU-43): barras solo JEFE; torta también ADMINISTRATIVO
        String barras = enviar("GET", "/api/estadisticas/pedidos-por-preventista?mes=" + YearMonth.now(), jefe, null, 200);
        assertEquals(YearMonth.now().lengthOfMonth(), (int) JsonPath.read(barras, "$.dias.length()"));
        enviar("GET", "/api/estadisticas/pedidos-por-preventista", administrativo, null, 403);
        enviar("GET", "/api/estadisticas/pedidos-por-preventista?mes=2026-13", jefe, null, 400);
        String torta = enviar("GET", "/api/estadisticas/stock-por-categoria", administrativo, null, 200);
        assertEquals("Golosinas", JsonPath.read(torta, "$[0].categoria"));
        assertEquals(7, ((Number) JsonPath.read(torta, "$[0].bultos")).intValue());

        // El DNI no se modifica desde administración (se valida en el backend)
        String cambioDni = """
                {"nombre":"Juan","apellido":"Pérez","fechaNacimiento":"1990-05-20","tipoDocumento":"DNI",
                 "numeroDocumento":"39999999","rol":"ADMINISTRATIVO",
                 "direccion":{"calle":"Colón","numeracion":"100","idLocalidad":"%s"}}
                """.formatted(ciudad);
        enviar("PUT", "/api/empleados/" + idAdministrativo, jefe, cambioDni, 400);
        assertEquals("30111555", JsonPath.read(enviar("GET", "/api/empleados/" + idAdministrativo, jefe, null, 200), "$.numeroDocumento"));

        // Renovación de la sesión: con token válido devuelve otro; sin token, 401
        String renovado = enviar("POST", "/api/auth/renovar", administrativo, null, 200);
        assertFalse(((String) JsonPath.read(renovado, "$.token")).isBlank());
        enviar("POST", "/api/auth/renovar", null, null, 401);
        // La respuesta de sesión no expone la clave
        assertFalse(renovado.contains("clave\":\"") || renovado.contains(CLAVE));
    }
}
