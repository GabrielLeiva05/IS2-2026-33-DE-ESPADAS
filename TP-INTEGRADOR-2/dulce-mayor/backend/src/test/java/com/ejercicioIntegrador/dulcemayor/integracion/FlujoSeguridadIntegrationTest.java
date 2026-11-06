package com.ejercicioIntegrador.dulcemayor.integracion;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.model.ReservaStock;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPago;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPuntoVenta;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private RepositorioPuntoVenta repositorioPuntoVenta;

    @Autowired
    private RepositorioPedido repositorioPedido;

    @Autowired
    private RepositorioStock repositorioStock;

    @Autowired
    private RepositorioReservaStock repositorioReservaStock;

    @Autowired
    private RepositorioPago repositorioPago;

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
