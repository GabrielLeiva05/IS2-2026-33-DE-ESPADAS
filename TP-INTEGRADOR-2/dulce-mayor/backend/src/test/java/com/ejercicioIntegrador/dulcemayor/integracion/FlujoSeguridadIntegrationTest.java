package com.ejercicioIntegrador.dulcemayor.integracion;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.model.ReservaStock;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioEmpleado;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPago;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioReservaStock;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioStock;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioUsuario;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de punta a punta con la cadena de Spring Security real y H2 en memoria:
 * jefe inicial -> cambio obligatorio de clave -> alta de empleados -> permisos por rol ->
 * registro de clientes -> catálogo público. Basada en FlujoUsuarioIntegrationTests de Blockazo.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlujoSeguridadIntegrationTest {

    private static final String JEFE = "jefe@dulcemayor.test";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private RepositorioPedido repositorioPedido;

    @Autowired
    private RepositorioStock repositorioStock;

    @Autowired
    private RepositorioReservaStock repositorioReservaStock;

    @Autowired
    private RepositorioPago repositorioPago;

    @Autowired
    private RepositorioEmpleado repositorioEmpleado;

    @Autowired
    private ServicioPago servicioPago;

    private MockMvc mvc;

    private String preventistaHabilitado() throws Exception {
        return empleadoHabilitado(jefeHabilitado(), "PREVENTISTA", "flujo@dulcemayor.test", "30111991", "FlujoSeguro2026");
    }

    /** Da de alta un empleado con el rol indicado y devuelve el token ya habilitado (clave cambiada). */
    private String empleadoHabilitado(String jefe, String rol, String correo, String dni, String clave) throws Exception {
        String idLocalidad = idCiudadDeMendoza(jefe);
        mvc.perform(post("/api/empleados").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content(empleadoJson(rol, correo, dni, idLocalidad)))
                .andExpect(status().isCreated());
        return cambiarClave(login(correo, "dulcemayor"), "dulcemayor", clave);
    }

    private void crearStock(String jefe, String idProducto, int cantidadInicial) throws Exception {
        mvc.perform(post("/api/stock").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":\"" + idProducto + "\",\"cantidadInicial\":" + cantidadInicial
                                + ",\"stockMinimo\":2}"))
                .andExpect(status().isCreated());
    }

    private String crearProductoConPrecio(String jefe, String codigo, String nombre, double precio) throws Exception {
        String categoria = mvc.perform(post("/api/categorias").header("Authorization", jefe)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"" + codigo + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idCategoria = JsonPath.read(categoria, "$.id");
        String subCategoria = mvc.perform(post("/api/subcategorias").header("Authorization", jefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + codigo + " sub\",\"idCategoria\":\"" + idCategoria + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String imagen = mvc.perform(post("/api/imagenes").header("Authorization", jefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + codigo + ".png\",\"mime\":\"image/png\",\"contenido\":\"AQID\",\"tipoImagen\":\"PRODUCTO\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String producto = mvc.perform(post("/api/productos").header("Authorization", jefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"nombre\":\"" + nombre
                                + "\",\"presentacion\":\"UNIDAD\",\"unidadesPorPresentacion\":1,\"enOferta\":false,\"idSubCategoria\":\""
                                + JsonPath.read(subCategoria, "$.id") + "\",\"idsImagen\":[\"" + JsonPath.read(imagen, "$.id") + "\"]}"))
                                                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idProducto = JsonPath.read(producto, "$.id");
        mvc.perform(post("/api/vigencias-precio").header("Authorization", jefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaDesde\":\"2020-01-01\",\"precio\":" + precio
                                + ",\"idProducto\":\"" + idProducto + "\"}"))
                .andExpect(status().isCreated());
        return idProducto;
    }

    private String pedidoJson(String idPuntoVenta, String idProducto, int cantidad) {
        return "{\"idPuntoVenta\":\"" + idPuntoVenta + "\",\"lineas\":[{\"idProducto\":\""
                + idProducto + "\",\"cantidadPresentaciones\":" + cantidad + "}]}";
    }


    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    // ------------------------------------------------------------------ auxiliares

    private String login(String correo, String clave) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(cuerpo, "$.token");
    }

    private String cambiarClave(String token, String actual, String nueva) throws Exception {
        String cuerpo = mvc.perform(put("/api/auth/clave").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claveActual\":\"" + actual + "\",\"nuevaClave\":\"" + nueva
                                + "\",\"confirmarClave\":\"" + nueva + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.debeCambiarClave").value(false))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(cuerpo, "$.token");
    }

    private String jefeHabilitado() throws Exception {
        return cambiarClave(login(JEFE, "dulcemayor"), "dulcemayor", "JefeSeguro2026");
    }

    /** Navega Argentina > Mendoza > Capital y devuelve el id de la Ciudad de Mendoza (datos iniciales). */
    private String idCiudadDeMendoza(String token) throws Exception {
        String paises = mvc.perform(get("/api/paises").header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String idPais = JsonPath.read(paises, "$[0].id");
        String provincias = mvc.perform(get("/api/provincias/pais/" + idPais).header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        String idProvincia = JsonPath.read(provincias, "$[0].id");
        String departamentos = mvc.perform(get("/api/departamentos/provincia/" + idProvincia).header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        List<String> idsCapital = JsonPath.read(departamentos, "$[?(@.nombre == 'Capital')].id");
        String localidades = mvc.perform(get("/api/localidades/departamento/" + idsCapital.get(0)).header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(localidades, "$[0].id");
    }

    private String empleadoJson(String rol, String correo, String dni, String idLocalidad) {
        return """
                {"nombre":"Ana","apellido":"Pérez","fechaNacimiento":"1990-05-20","tipoDocumento":"DNI",
                 "numeroDocumento":"%s","correo":"%s","rol":"%s","zona":"Zona Este",
                 "vehiculo":"Fiat Fiorino","patente":"AB123CD",
                 "direccion":{"calle":"San Martín","numeracion":"1234","idLocalidad":"%s"}}
                """.formatted(dni, correo, rol, idLocalidad);
    }

    private String registrarPuntoVenta(String preventista, String idLocalidad, String documento) throws Exception {
        String cuerpo = mvc.perform(post("/api/preventista/puntos-venta").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreCliente":"Carlos","apellidoCliente":"Gómez","correoContactoCliente":"carlos@example.test",
                                 "fechaNacimientoCliente":"1985-03-12","tipoDocumentoCliente":"DNI","numeroDocumentoCliente":"%s",
                                 "nombreComercial":"Kiosco Norte","telefono":"2615551234","emailContacto":"kiosco@example.test",
                                 "direccion":{"calle":"San Martín","numeracion":"123","latitud":-32.89,"longitud":-68.84,"idLocalidad":"%s"}}
                                """.formatted(documento, idLocalidad)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.id");
    }

    // ------------------------------------------------------------------ pruebas

    @Test
    void elJefeInicialDebeCambiarLaClaveTemporalAntesDeOperar() throws Exception {
        String temporal = login(JEFE, "dulcemayor");

        mvc.perform(get("/api/empleados").header("Authorization", temporal))
                .andExpect(status().isForbidden())
                .andExpect(header().string("X-Accion-Requerida", "CAMBIAR_CLAVE"));
        mvc.perform(get("/api/auth/yo").header("Authorization", temporal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debeCambiarClave").value(true));

        String habilitado = cambiarClave(temporal, "dulcemayor", "JefeSeguro2026");

        mvc.perform(get("/api/empleados").header("Authorization", habilitado)).andExpect(status().isOk());
        // La clave quedó guardada con BCrypt, nunca en texto plano
        String hash = repositorioUsuario.findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse(JEFE).orElseThrow().getClave();
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void elJefeDaDeAltaUnPreventistaQueSoloOperaTrasCambiarSuClave() throws Exception {
        String jefe = jefeHabilitado();
        String idLocalidad = idCiudadDeMendoza(jefe);

        mvc.perform(post("/api/empleados").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content(empleadoJson("PREVENTISTA", "ana@dulcemayor.test", "30111222", idLocalidad)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PREVENTISTA"))
                .andExpect(jsonPath("$.debeCambiarClave").value(true))
                .andExpect(jsonPath("$.clave").doesNotExist());

        String temporal = login("ana@dulcemayor.test", "dulcemayor");
        mvc.perform(get("/api/productos").header("Authorization", temporal)).andExpect(status().isForbidden());

        String preventista = cambiarClave(temporal, "dulcemayor", "Preventa2026");
        mvc.perform(get("/api/productos").header("Authorization", preventista)).andExpect(status().isOk());
        mvc.perform(post("/api/categorias").header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Chicles\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/empleados").header("Authorization", preventista)).andExpect(status().isForbidden());
    }

    @Test
    void elJefeNoPuedeCrearOtroJefeYElAdministrativoGestionaElCatalogo() throws Exception {
        String jefe = jefeHabilitado();
        String idLocalidad = idCiudadDeMendoza(jefe);

        mvc.perform(post("/api/empleados").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content(empleadoJson("JEFE", "jefe2@dulcemayor.test", "30111333", idLocalidad)))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/empleados").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content(empleadoJson("ADMINISTRATIVO", "admin@dulcemayor.test", "30111444", idLocalidad)))
                .andExpect(status().isCreated());
        String administrativo = cambiarClave(login("admin@dulcemayor.test", "dulcemayor"), "dulcemayor", "Almacen2026");

        mvc.perform(post("/api/categorias").header("Authorization", administrativo).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Chocolates\"}")).andExpect(status().isCreated());
        // El jefe hereda los permisos del administrativo
        mvc.perform(post("/api/categorias").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Alfajores\"}")).andExpect(status().isCreated());
        // Pero el administrativo no gestiona empleados
        mvc.perform(get("/api/empleados").header("Authorization", administrativo)).andExpect(status().isForbidden());
    }

    @Test
    void elFlujoComercialCreaResponsablePedidoReservaYLiberaAlCancelar() throws Exception {
        String preventista = preventistaHabilitado();
        String jefe = login(JEFE, "JefeSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "40123456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-01", "Alfajor flujo", 125.50);
        mvc.perform(post("/api/stock").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":\"" + idProducto + "\",\"cantidadInicial\":10,\"stockMinimo\":2}"))
                .andExpect(status().isCreated());

        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.total").value(376.50))
                .andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");

        mvc.perform(put("/api/preventista/pedidos/" + idPedido + "/lineas").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 2)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(251.00));

        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CONFIRMADO"));
        assertEquals(10, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());
        assertEquals(2, repositorioReservaStock.findByPedido_IdAndEliminadoFalse(idPedido).get(0).getCantidad());

        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/cancelar").header("Authorization", preventista))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADO"));
        assertEquals(10, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());
        assertTrue(repositorioReservaStock.findByPedido_IdAndEliminadoFalse(idPedido).get(0).getFechaLiberacion() != null);
    }

    @Test
    void elPedidoConfirmadoSoloConsumeStockAlAcreditarseElPago() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "flujo@dulcemayor.test", "30111991", "FlujoSeguro2026");
        String administrativo = empleadoHabilitado(jefe, "ADMINISTRATIVO", "admin@dulcemayor.test", "30111444", "Almacen2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "40223456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-02", "Gomitas flujo", 80.00);
        crearStock(jefe, idProducto, 10);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 3)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");

        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CONFIRMADO"));

        // Iniciar el cobro no descuenta el stock físico: sigue reservado
        String pagoJson = mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/pagos")
                        .header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medio\":\"TRANSFERENCIA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.instrucciones").isNotEmpty())
                .andExpect(jsonPath("$.importe").value(240.00))
                .andReturn().getResponse().getContentAsString();
        String idPago = JsonPath.read(pagoJson, "$.id");
        assertEquals(10, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());

        // El preventista no puede acreditar el pago: es conciliación administrativa
        mvc.perform(post("/api/pagos/" + idPago + "/acreditar").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"referencia\":\"TR-1\"}"))
                .andExpect(status().isForbidden());
        // La transferencia exige número de operación
        mvc.perform(post("/api/pagos/" + idPago + "/acreditar").header("Authorization", administrativo)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/pagos/" + idPago + "/acreditar").header("Authorization", administrativo)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"referencia\":\"TR-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACREDITADO"))
                .andExpect(jsonPath("$.fechaAcreditacion").exists());

        assertEquals(7, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());
        ReservaStock consumida = repositorioReservaStock.findByPedido_IdAndEliminadoFalse(idPedido).get(0);
        assertTrue(consumida.isConsumida());
        assertNotNull(consumida.getFechaConsumo());
        assertNull(consumida.getFechaLiberacion());

        mvc.perform(get("/api/pedidos/" + idPedido).header("Authorization", administrativo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("COMPLETADO"));

        // Comprobante interno descargable para administración
        mvc.perform(get("/api/pedidos/" + idPedido + "/comprobante").header("Authorization", administrativo))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Comprobante interno no fiscal")));
        // Y también para el preventista dueño del pedido
        mvc.perform(get("/api/preventista/pedidos/" + idPedido + "/comprobante").header("Authorization", preventista))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gomitas flujo")));

        // Acreditar de nuevo es idempotente y no vuelve a consumir stock
        mvc.perform(post("/api/pagos/" + idPago + "/acreditar").header("Authorization", administrativo)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"referencia\":\"TR-1\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ACREDITADO"));
        assertEquals(7, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());
    }

    @Test
    void laPromocionSeCongelaEnElPedidoYElPagoUsaElTotalConDescuento() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "promo@dulcemayor.test", "30111999", "FlujoSeguro2026");
        String administrativo = empleadoHabilitado(jefe, "ADMINISTRATIVO", "admin3@dulcemayor.test", "30111446", "Almacen2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "40923456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-09", "Alfajor promo", 100.00);
        crearStock(jefe, idProducto, 10);

        mvc.perform(post("/api/promociones").header("Authorization", administrativo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"promo10","descripcion":"10% en alfajores","tipo":"PORCENTAJE","valor":10,
                                 "fechaDesde":"2026-01-01","activa":true,"usoUnico":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("PROMO10"));

        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(300.00))
                .andExpect(jsonPath("$.total").value(300.00))
                .andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");

        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/promocion")
                        .header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"promo10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promocionCodigo").value("PROMO10"))
                .andExpect(jsonPath("$.descuento").value(30.00))
                .andExpect(jsonPath("$.total").value(270.00));

        // Un código inexistente no se aplica y un segundo código no acumula
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/promocion")
                        .header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"NOEXISTE\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(270.00));

        String pagoJson = mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/pagos")
                        .header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medio\":\"TRANSFERENCIA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importe").value(270.00))
                .andReturn().getResponse().getContentAsString();
        String idPago = JsonPath.read(pagoJson, "$.id");

        mvc.perform(post("/api/pagos/" + idPago + "/acreditar").header("Authorization", administrativo)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"referencia\":\"TR-PROMO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ACREDITADO"));
        assertEquals(7, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());

        // El código de uso único ya consumido no se puede volver a usar
        String otroBorrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String otroPedido = JsonPath.read(otroBorrador, "$.id");
        mvc.perform(post("/api/preventista/pedidos/" + otroPedido + "/promocion")
                        .header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"PROMO10\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("ya fue utilizado")));
    }

    @Test
    void elPagoVencidoLiberaLaReservaYCancelaElPedido() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "vence@dulcemayor.test", "30111997", "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "40723456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-07", "Alfajor vencimiento", 60.00);
        crearStock(jefe, idProducto, 8);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 2)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk());
        String pagoJson = mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/pagos")
                        .header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medio\":\"TRANSFERENCIA\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String idPago = JsonPath.read(pagoJson, "$.id");

        // Se fuerza el vencimiento para no esperar 24 h reales
        Pago pago = repositorioPago.findById(idPago).orElseThrow();
        pago.setFechaVencimiento(LocalDateTime.now().minusMinutes(5));
        repositorioPago.save(pago);

        assertTrue(servicioPago.expirar(idPago));

        assertEquals(EstadoPago.EXPIRADO, repositorioPago.findById(idPago).orElseThrow().getEstado());
        assertEquals(EstadoPedido.CANCELADO,
                repositorioPedido.findById(idPedido).orElseThrow().getEstado());
        assertNotNull(repositorioReservaStock.findByPedido_IdAndEliminadoFalse(idPedido).get(0).getFechaLiberacion());
        assertEquals(8, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());

        // Un pago acreditado después del vencimiento ya no aplica
        mvc.perform(post("/api/pagos/" + idPago + "/acreditar").header("Authorization", jefe)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"referencia\":\"TR-9\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laEntregaConCobroEnEfectivoCierraElPedidoYConsumeElStock() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "ruta@dulcemayor.test", "30112001", "FlujoSeguro2026");
        String distribuidor = empleadoHabilitado(jefe, "DISTRIBUIDOR", "reparto@dulcemayor.test", "30112002", "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "41023456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-10", "Alfajor ruta", 50.00);
        crearStock(jefe, idProducto, 10);

        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 2)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk());
        // Cobro pactado en efectivo al entregar
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/pagos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"medio\":\"EFECTIVO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("PENDIENTE"));

        String idDistribuidor = idEmpleadoDe("reparto@dulcemayor.test");
        String rutaJson = mvc.perform(post("/api/rutas").header("Authorization", jefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idDistribuidor\":\"" + idDistribuidor + "\",\"idsPedidos\":[\"" + idPedido + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paradas.length()").value(1))
                .andExpect(jsonPath("$.paradas[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.paradas[0].calle").value("San Martín"))
                .andExpect(jsonPath("$.idSiguienteParada").exists())
                .andReturn().getResponse().getContentAsString();
        String idParada = JsonPath.read(rutaJson, "$.paradas[0].id");

        // El preventista no puede operar la hoja de ruta
        mvc.perform(get("/api/distribuidor/ruta").header("Authorization", preventista))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/distribuidor/ruta").header("Authorization", distribuidor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paradas[0].id").value(idParada));

        mvc.perform(post("/api/distribuidor/paradas/" + idParada + "/entregar").header("Authorization", distribuidor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paradas[0].estado").value("ENTREGADO"))
                .andExpect(jsonPath("$.paradas[0].cobroEfectivo").value(true))
                .andExpect(jsonPath("$.estado").value("CERRADA"))
                .andExpect(jsonPath("$.idSiguienteParada").doesNotExist());

        // Cobrar en la entrega acreditó el pago y consumió el stock
        assertEquals(8, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());
        assertEquals(EstadoPedido.ENTREGADO, repositorioPedido.findById(idPedido).orElseThrow().getEstado());
        mvc.perform(get("/api/pedidos/" + idPedido + "/comprobante").header("Authorization", jefe))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comprobante interno no fiscal")));

        // Una parada resuelta no se entrega dos veces
        mvc.perform(post("/api/distribuidor/paradas/" + idParada + "/entregar").header("Authorization", distribuidor))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elIntentoFallidoQuedaRegistradoYPermiteReasignarElPedido() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "fallo@dulcemayor.test", "30112003", "FlujoSeguro2026");
        String distribuidor = empleadoHabilitado(jefe, "DISTRIBUIDOR", "fallo-reparto@dulcemayor.test", "30112004", "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "41123456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-11", "Turrón ruta", 40.00);
        crearStock(jefe, idProducto, 5);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk());

        String idDistribuidor = idEmpleadoDe("fallo-reparto@dulcemayor.test");
        String rutaJson = mvc.perform(post("/api/rutas").header("Authorization", jefe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idDistribuidor\":\"" + idDistribuidor + "\",\"idsPedidos\":[\"" + idPedido + "\"]}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String idParada = JsonPath.read(rutaJson, "$.paradas[0].id");

        // El motivo es obligatorio
        mvc.perform(post("/api/distribuidor/paradas/" + idParada + "/fallar").header("Authorization", distribuidor)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/distribuidor/paradas/" + idParada + "/fallar").header("Authorization", distribuidor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Comercio cerrado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paradas[0].estado").value("FALLIDO"))
                .andExpect(jsonPath("$.paradas[0].motivoFallo").value("Comercio cerrado"));

        // El pedido sigue confirmado y reservado: se puede reprogramar en otra hoja de ruta
        assertEquals(EstadoPedido.CONFIRMADO, repositorioPedido.findById(idPedido).orElseThrow().getEstado());
        assertEquals(5, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());
        mvc.perform(post("/api/rutas").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idDistribuidor\":\"" + idDistribuidor + "\",\"idsPedidos\":[\"" + idPedido + "\"],"
                                + "\"fecha\":\"" + LocalDate.now().plusDays(1) + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paradas[0].estado").value("PENDIENTE"));
    }

    private String idEmpleadoDe(String correo) {
        String idUsuario = repositorioUsuario.findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse(correo)
                .orElseThrow().getId();
        return repositorioEmpleado.findFirstByUsuario_Id(idUsuario).orElseThrow().getId();
    }

    @Test
    void elPortalDelClienteVeSusPedidosYElSitioPublicoExponeSucursalesYCotizacion() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "portal@dulcemayor.test", "30112005", "FlujoSeguro2026");
        String administrativo = empleadoHabilitado(jefe, "ADMINISTRATIVO", "admin4@dulcemayor.test", "30111447", "Almacen2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "41223456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-12", "Bombón portal", 70.00);
        crearStock(jefe, idProducto, 6);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 2)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk());
        String pagoJson = mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/pagos")
                        .header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medio\":\"TRANSFERENCIA\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/pagos/" + JsonPath.read(pagoJson, "$.id") + "/acreditar")
                        .header("Authorization", administrativo).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"referencia\":\"TR-PORTAL\"}"))
                .andExpect(status().isOk());

        // El cliente se registra con el correo de contacto del comercio
        String sesionCliente = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Carlos","apellido":"Gómez","correo":"kiosco@example.test","clave":"Golosinas2026",
                                 "confirmacionClave":"Golosinas2026"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andReturn().getResponse().getContentAsString();
        String cliente = "Bearer " + JsonPath.read(sesionCliente, "$.token");

        mvc.perform(get("/api/cliente/pedidos").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(idPedido))
                .andExpect(jsonPath("$[0].estado").value("COMPLETADO"));
        mvc.perform(get("/api/cliente/pedidos/" + idPedido + "/comprobante").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comprobante interno no fiscal")));
        // El preventista no puede usar el portal del cliente
        mvc.perform(get("/api/cliente/pedidos").header("Authorization", preventista))
                .andExpect(status().isForbidden());

        // Sucursales: públicas para leer, administrativas para gestionar
        mvc.perform(get("/api/sucursales")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(post("/api/sucursales").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(sucursalJson(idLocalidad)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/sucursales").header("Authorization", administrativo)
                        .contentType(MediaType.APPLICATION_JSON).content(sucursalJson(idLocalidad)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Sucursal Centro"));
        mvc.perform(get("/api/sucursales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].direccion.localidad").value("Ciudad de Mendoza"));

        // Cotización: responde siempre, aunque el BCRA no esté disponible
        mvc.perform(get("/api/cotizacion-dolar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actualizada").exists())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void elPreventistaPlanificaYReordenaSusVisitas() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "visitas@dulcemayor.test", "30112006", "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);

        // Sin cartera no hay plan posible
        mvc.perform(post("/api/preventista/visitas/generar").header("Authorization", preventista))
                .andExpect(status().isBadRequest());

        registrarPuntoVenta(preventista, idLocalidad, "41323456");
        String segundoPunto = mvc.perform(post("/api/preventista/puntos-venta").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreCliente":"Lucía","apellidoCliente":"Fernández","correoContactoCliente":"lucia@example.test",
                                 "tipoDocumentoCliente":"DNI","numeroDocumentoCliente":"41423456",
                                 "nombreComercial":"Almacén Sur","telefono":"2615559999","emailContacto":"sur@example.test",
                                 "direccion":{"calle":"Belgrano","numeracion":"500","latitud":-32.95,"longitud":-68.90,"idLocalidad":"%s"}}
                                """.formatted(idLocalidad)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPuntoSur = JsonPath.read(segundoPunto, "$.id");

        String planJson = mvc.perform(post("/api/preventista/visitas/generar").header("Authorization", preventista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitas.length()").value(2))
                .andExpect(jsonPath("$.visitas[0].orden").value(1))
                .andReturn().getResponse().getContentAsString();
        List<String> idsVisitas = JsonPath.read(planJson, "$.visitas[*].id");
        List<String> puntosDelPlan = JsonPath.read(planJson, "$.visitas[*].idPuntoVenta");
        List<String> nombres = JsonPath.read(planJson, "$.visitas[*].nombreComercial");
        assertTrue(nombres.contains("Kiosco Norte") && nombres.contains("Almacén Sur"));

        // Reordenar invierte el recorrido
        mvc.perform(put("/api/preventista/visitas/orden").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idsVisitas\":[\"" + idsVisitas.get(1) + "\",\"" + idsVisitas.get(0) + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitas[0].id").value(idsVisitas.get(1)))
                .andExpect(jsonPath("$.visitas[0].orden").value(1))
                .andExpect(jsonPath("$.visitas[0].idPuntoVenta").value(puntosDelPlan.get(1)));

        mvc.perform(get("/api/preventista/visitas").header("Authorization", preventista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitas[0].id").value(idsVisitas.get(1)));

        // Otro rol no accede a la planificación
        String otro = empleadoHabilitado(jefe, "ADMINISTRATIVO", "admin5@dulcemayor.test", "30111448", "Almacen2026");
        mvc.perform(get("/api/preventista/visitas").header("Authorization", otro))
                .andExpect(status().isForbidden());
    }

    private String sucursalJson(String idLocalidad) {
        return """
                {"nombre":"Sucursal Centro","telefono":"2615550000","emailContacto":"centro@dulcemayor.test",
                 "horario":"Lunes a viernes de 9 a 18",
                 "direccion":{"calle":"San Martín","numeracion":"100","barrio":"Centro","latitud":-32.89,"longitud":-68.84,
                              "idLocalidad":"%s"}}
                """.formatted(idLocalidad);
    }

    @Test
    void elListadoDeClientesIncluyeComerciosSinCuentaDeAcceso() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "clientes@dulcemayor.test", "30113001",
                "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);

        // El alta del preventista crea el cliente sin cuenta de acceso (no hay verificación de correo):
        // el listado administrativo debe funcionar igual y mostrar el correo en null.
        registrarPuntoVenta(preventista, idLocalidad, "42345678");

        mvc.perform(get("/api/clientes").header("Authorization", jefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.numeroDocumento == '42345678')].nombre").exists())
                .andExpect(jsonPath("$[?(@.numeroDocumento == '42345678')].correo").value(org.hamcrest.Matchers.contains((Object) null)));
    }

    @Test
    void mercadoPagoSinConfigurarNoSimulaAcreditacionYElWebhookExigeFirma() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "mp@dulcemayor.test", "30111998", "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "40823456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-08", "Chocolate mp", 90.00);
        crearStock(jefe, idProducto, 5);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk());

        // Sin MERCADOPAGO_ACCESS_TOKEN el servidor avisa; no inventa una preferencia
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/pagos")
                        .header("Authorization", preventista).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medio\":\"MERCADO_PAGO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("no está configurado")));
        assertTrue(repositorioPago.findByPedido_IdAndEliminadoFalseOrderByFechaCreacionDesc(idPedido).isEmpty());

        // El webhook es público pero exige firma válida
        mvc.perform(post("/api/pagos/mercadopago/notificacion?data.id=123&type=payment")
                        .header("x-signature", "ts=1,v1=invalida")
                        .header("x-request-id", "req-1"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/pagos/mercadopago/notificacion?data.id=123&type=payment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elCicloAdministrativoEstaCerradoAlPreventistaYElAdministrativoNoCargaPedidos() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "roles@dulcemayor.test", "30111993", "FlujoSeguro2026");
        String administrativo = empleadoHabilitado(jefe, "ADMINISTRATIVO", "admin2@dulcemayor.test", "30111445", "Almacen2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "40423456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-04", "Caramelo flujo", 30.00);
        crearStock(jefe, idProducto, 5);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk());

        // El preventista no cierra pedidos por la vía administrativa
        mvc.perform(post("/api/pedidos/" + idPedido + "/completar").header("Authorization", preventista))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/pedidos/" + idPedido + "/cancelar").header("Authorization", preventista))
                .andExpect(status().isForbidden());
        // El administrativo no crea ni lista pedidos de preventista
        mvc.perform(post("/api/preventista/pedidos").header("Authorization", administrativo)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 1)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/preventista/pedidos").header("Authorization", administrativo))
                .andExpect(status().isForbidden());

        // El jefe hereda el cierre administrativo y libera la reserva
        mvc.perform(post("/api/pedidos/" + idPedido + "/cancelar").header("Authorization", jefe))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADO"));
        assertNotNull(repositorioReservaStock.findByPedido_IdAndEliminadoFalse(idPedido).get(0).getFechaLiberacion());
    }

    @Test
    void laConfirmacionRechazaSobreventaYNoDejaReservasParciales() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "sobreventa@dulcemayor.test", "30111992", "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "40323456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-03", "Chupetín flujo", 50.00);
        crearStock(jefe, idProducto, 2);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 3)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");

        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("Stock insuficiente")));

        assertTrue(repositorioReservaStock.findByPedido_IdAndEliminadoFalse(idPedido).isEmpty());
        assertEquals(2, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());
        mvc.perform(get("/api/preventista/pedidos").header("Authorization", preventista))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].estado").value("BORRADOR"));
    }

    @Test
    void unPreventistaNoVeNiOperaLaCarteraDeOtro() throws Exception {
        String jefe = jefeHabilitado();
        String titular = empleadoHabilitado(jefe, "PREVENTISTA", "titular@dulcemayor.test", "30111994", "FlujoSeguro2026");
        String ajeno = empleadoHabilitado(jefe, "PREVENTISTA", "ajeno@dulcemayor.test", "30111995", "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(titular, idLocalidad, "40523456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-05", "Turrón flujo", 20.00);
        crearStock(jefe, idProducto, 5);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", titular)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");

        mvc.perform(get("/api/preventista/pedidos").header("Authorization", ajeno))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(get("/api/preventista/puntos-venta").header("Authorization", ajeno))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", ajeno))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/preventista/pedidos").header("Authorization", ajeno)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 1)))
                .andExpect(status().isBadRequest());

        assertTrue(repositorioReservaStock.findByPedido_IdAndEliminadoFalse(idPedido).isEmpty());
        assertEquals(5, repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).orElseThrow().getCantidadActual());
    }

    @Test
    void elPrecioConfirmadoQuedaCongeladoAunqueCambieLaVigencia() throws Exception {
        String jefe = jefeHabilitado();
        String preventista = empleadoHabilitado(jefe, "PREVENTISTA", "precio@dulcemayor.test", "30111996", "FlujoSeguro2026");
        String idLocalidad = idCiudadDeMendoza(jefe);
        String idPuntoVenta = registrarPuntoVenta(preventista, idLocalidad, "40623456");
        String idProducto = crearProductoConPrecio(jefe, "FLUJO-06", "Alfajor precio", 100.00);
        crearStock(jefe, idProducto, 10);
        String borrador = mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(200.00))
                .andReturn().getResponse().getContentAsString();
        String idPedido = JsonPath.read(borrador, "$.id");
        mvc.perform(post("/api/preventista/pedidos/" + idPedido + "/confirmar").header("Authorization", preventista))
                .andExpect(status().isOk());

        mvc.perform(post("/api/vigencias-precio").header("Authorization", jefe).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaDesde\":\"" + LocalDate.now() + "\",\"precio\":150.00,\"idProducto\":\""
                                + idProducto + "\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/preventista/pedidos").header("Authorization", preventista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(200.00))
                .andExpect(jsonPath("$[0].lineas[0].precioUnitario").value(100.00));

        // Un pedido nuevo sí toma el precio vigente actualizado
        mvc.perform(post("/api/preventista/pedidos").header("Authorization", preventista)
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson(idPuntoVenta, idProducto, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(300.00));
    }

    @Test
    void elClienteSeRegistraConSuClaveYNoPuedeElegirSuRol() throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Marta","apellido":"Suárez","correo":"Marta@Kiosco.com","clave":"Golosinas2026",
                                 "confirmacionClave":"Golosinas2026","rol":"JEFE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.debeCambiarClave").value(false))
                .andExpect(jsonPath("$.usuario.correo").value("marta@kiosco.com"))
                .andReturn().getResponse().getContentAsString();
        String cliente = "Bearer " + JsonPath.read(cuerpo, "$.token");

        mvc.perform(get("/api/clientes/yo").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Marta"));
        mvc.perform(get("/api/empleados").header("Authorization", cliente)).andExpect(status().isForbidden());
        mvc.perform(post("/api/categorias").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"X\"}")).andExpect(status().isForbidden());

        // Puede volver a ingresar con su clave, sin cambio obligatorio
        login("marta@kiosco.com", "Golosinas2026");
    }

    @Test
    void sinTokenSoloSeAccedeALoPublico() throws Exception {
        mvc.perform(get("/api/catalogo")).andExpect(status().isOk());
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mvc.perform(get("/api/empleados")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"X\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + JEFE + "\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }
}



