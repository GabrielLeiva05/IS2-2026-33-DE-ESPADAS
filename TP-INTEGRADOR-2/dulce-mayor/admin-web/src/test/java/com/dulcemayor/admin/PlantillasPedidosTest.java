package com.dulcemayor.admin;

import com.dulcemayor.admin.dto.LineaPedidoAdminDTO;
import com.dulcemayor.admin.dto.PagoAdminDTO;
import com.dulcemayor.admin.dto.PedidoAdminDTO;
import com.dulcemayor.admin.dto.SesionApiDTO;
import com.dulcemayor.admin.dto.UsuarioSesionApiDTO;
import com.dulcemayor.admin.seguridad.SesionUsuario;
import com.dulcemayor.admin.service.ServicioPedidosAdmin;
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
