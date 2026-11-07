package com.dulcemayor.admin;

import com.dulcemayor.admin.dto.LineaPedidoAdminDTO;
import com.dulcemayor.admin.dto.PagoAdminDTO;
import com.dulcemayor.admin.dto.ParadaAdminDTO;
import com.dulcemayor.admin.dto.PedidoAdminDTO;
import com.dulcemayor.admin.dto.PromocionAdminDTO;
import com.dulcemayor.admin.dto.RutaAdminDTO;
import com.dulcemayor.admin.dto.SesionApiDTO;
import com.dulcemayor.admin.dto.UsuarioSesionApiDTO;
import com.dulcemayor.admin.seguridad.SesionUsuario;
import com.dulcemayor.admin.service.ServicioPedidosAdmin;
import com.dulcemayor.admin.service.ServicioPromocionesAdmin;
import com.dulcemayor.admin.service.ServicioRutasAdmin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Renderiza las pantallas nuevas con el motor real (MockMvc + Thymeleaf), con el servicio de la API
 * simulado: así se detectan errores de plantilla que la compilación de Java no ve.
 */
@SpringBootTest
class PlantillasPedidosTest {

    private static final String ATRIBUTO_SESION = "DM_SESION_EMPLEADO";

    @Autowired
    private WebApplicationContext contexto;

    @MockitoBean
    private ServicioPedidosAdmin servicioPedidos;

    @MockitoBean
    private ServicioPromocionesAdmin servicioPromociones;

    @MockitoBean
    private ServicioRutasAdmin servicioRutas;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void laBandejaMuestraLosPedidosYElEstadoVacio() throws Exception {
        when(servicioPedidos.listar("token-admin", null, null)).thenReturn(List.of(pedido()));

        mvc().perform(get("/pedidos").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kiosco Norte")))
                .andExpect(content().string(containsString("$ 240,00")))
                .andExpect(content().string(containsString("Confirmado (reservado)")));

        when(servicioPedidos.listar("token-admin", null, null)).thenReturn(List.of());
        mvc().perform(get("/pedidos").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no hay pedidos")));
    }

    @Test
    void elDetalleMuestraProductosPagosYAccionesDeConciliacion() throws Exception {
        when(servicioPedidos.detalle("token-admin", "ped-1")).thenReturn(pedido());
        when(servicioPedidos.pagos("token-admin", "ped-1")).thenReturn(List.of(pago("PENDIENTE")));
        when(servicioPedidos.tieneComprobante("token-admin", "ped-1")).thenReturn(false);

        mvc().perform(get("/pedidos/ped-1").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gomitas")))
                .andExpect(content().string(containsString("Confirmado (reservado)")))
                .andExpect(content().string(containsString("Acreditar")))
                .andExpect(content().string(containsString("Registrar cobro")))
                .andExpect(content().string(containsString("Cobro en efectivo")));
    }

    @Test
    void elDetalleDeUnPedidoCompletadoMuestraElComprobanteYNoOfreceCobrar() throws Exception {
        PedidoAdminDTO completado = pedido();
        completado.setEstado("COMPLETADO");
        when(servicioPedidos.detalle("token-admin", "ped-1")).thenReturn(completado);
        when(servicioPedidos.pagos("token-admin", "ped-1")).thenReturn(List.of(pago("ACREDITADO")));
        when(servicioPedidos.tieneComprobante("token-admin", "ped-1")).thenReturn(true);

        mvc().perform(get("/pedidos/ped-1").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comprobante")))
                .andExpect(content().string(containsString("Acreditado")));
    }

    @Test
    void laPantallaDePromocionesSeRenderizaConYsinCodigos() throws Exception {
        when(servicioPromociones.listar("token-admin")).thenReturn(List.of(promocion()));

        mvc().perform(get("/promociones").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("VERANO10")))
                .andExpect(content().string(containsString("10 %")))
                .andExpect(content().string(containsString("Uso único")))
                .andExpect(content().string(containsString("Crear promoción")));

        when(servicioPromociones.listar("token-admin")).thenReturn(List.of());
        mvc().perform(get("/promociones").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no hay códigos promocionales")));
    }

    @Test
    void laPantallaDeRutasSeRenderizaConParadasYFormularioDeAsignacion() throws Exception {
        RutaAdminDTO ruta = new RutaAdminDTO();
        ruta.setId("ruta-1");
        ruta.setFecha("2026-11-07");
        ruta.setNombreDistribuidor("Pedro Reparto");
        ruta.setVehiculo("Fiat Fiorino");
        ruta.setEstado("PLANIFICADA");
        ruta.setTotalCobrado(new BigDecimal("240.00"));
        ruta.setParadas(List.of(parada()));
        when(servicioRutas.listar("token-admin", null)).thenReturn(List.of(ruta));
        when(servicioRutas.distribuidores("token-admin")).thenReturn(List.of());
        when(servicioRutas.pedidosAsignables("token-admin")).thenReturn(List.of(pedido()));

        mvc().perform(get("/rutas").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pedro Reparto")))
                .andExpect(content().string(containsString("Kiosco Norte")))
                .andExpect(content().string(containsString("San Martín 123")))
                .andExpect(content().string(containsString("Crear / ampliar hoja de ruta")));
    }

    private ParadaAdminDTO parada() {
        ParadaAdminDTO parada = new ParadaAdminDTO();
        parada.setId("parada-1");
        parada.setNombreComercial("Kiosco Norte");
        parada.setCalle("San Martín");
        parada.setNumeracion("123");
        parada.setOrden(1);
        parada.setEstado("PENDIENTE");
        parada.setTotal(new BigDecimal("240.00"));
        return parada;
    }

    private PromocionAdminDTO promocion() {
        PromocionAdminDTO promocion = new PromocionAdminDTO();
        promocion.setId("promo-1");
        promocion.setCodigo("VERANO10");
        promocion.setDescripcion("10% en alfajores");
        promocion.setTipo("PORCENTAJE");
        promocion.setValor(new BigDecimal("10"));
        promocion.setFechaDesde("2026-01-01");
        promocion.setActiva(true);
        promocion.setUsoUnico(true);
        promocion.setVigente(true);
        return promocion;
    }

    private MockHttpSession sesionAdministrativa() {
        UsuarioSesionApiDTO usuario = new UsuarioSesionApiDTO();
        usuario.setCorreo("admin@dulcemayor.test");
        usuario.setRol("ADMINISTRATIVO");
        usuario.setNombre("Ana");
        usuario.setApellido("Pérez");
        SesionApiDTO api = new SesionApiDTO();
        api.setToken("token-admin");
        api.setUsuario(usuario);

        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(ATRIBUTO_SESION, SesionUsuario.desde(api));
        return sesion;
    }

    private PedidoAdminDTO pedido() {
        LineaPedidoAdminDTO linea = new LineaPedidoAdminDTO();
        linea.setNombreProducto("Gomitas");
        linea.setCantidadPresentaciones(3);
        linea.setPrecioUnitario(new BigDecimal("80.00"));
        linea.setSubtotal(new BigDecimal("240.00"));

        PedidoAdminDTO pedido = new PedidoAdminDTO();
        pedido.setId("ped-1");
        pedido.setNombreComercial("Kiosco Norte");
        pedido.setIdPuntoVenta("pv-1");
        pedido.setEstado("CONFIRMADO");
        pedido.setTotal(new BigDecimal("240.00"));
        pedido.setFechaCreacion("2026-11-06T10:00:00");
        pedido.setLineas(List.of(linea));
        return pedido;
    }

    private PagoAdminDTO pago(String estado) {
        PagoAdminDTO pago = new PagoAdminDTO();
        pago.setId("pag-1");
        pago.setIdPedido("ped-1");
        pago.setMedio("TRANSFERENCIA");
        pago.setEstado(estado);
        pago.setImporte(new BigDecimal("240.00"));
        pago.setFechaCreacion("2026-11-06T10:05:00");
        return pago;
    }
}
