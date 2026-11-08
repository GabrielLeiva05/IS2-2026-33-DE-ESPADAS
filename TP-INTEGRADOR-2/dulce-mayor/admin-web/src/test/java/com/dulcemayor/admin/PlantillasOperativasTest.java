package com.dulcemayor.admin;

import com.dulcemayor.admin.dto.CorreoAdminDTO;
import com.dulcemayor.admin.dto.FilaReporteAdminDTO;
import com.dulcemayor.admin.dto.PuntoVentaAdminDTO;
import com.dulcemayor.admin.dto.ReporteAdminDTO;
import com.dulcemayor.admin.dto.SesionApiDTO;
import com.dulcemayor.admin.dto.SuscripcionAdminDTO;
import com.dulcemayor.admin.dto.UsuarioSesionApiDTO;
import com.dulcemayor.admin.seguridad.SesionUsuario;
import com.dulcemayor.admin.service.ServicioNotificaciones;
import com.dulcemayor.admin.service.ServicioPuntosVenta;
import com.dulcemayor.admin.service.ServicioReportes;
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
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Renderiza las pantallas de reportes, notificaciones y puntos de venta con el motor real. */
@SpringBootTest
class PlantillasOperativasTest {

    private static final String ATRIBUTO_SESION = "DM_SESION_EMPLEADO";

    @Autowired
    private WebApplicationContext contexto;

    @MockitoBean
    private ServicioReportes servicioReportes;

    @MockitoBean
    private ServicioNotificaciones servicioNotificaciones;

    @MockitoBean
    private ServicioPuntosVenta servicioPuntosVenta;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void elReporteMuestraTotalesFilasYStock() throws Exception {
        ReporteAdminDTO reporte = new ReporteAdminDTO();
        reporte.setDesde("2026-11-01");
        reporte.setHasta("2026-11-30");
        reporte.setAgrupacion("departamento");
        reporte.setPedidos(12);
        reporte.setPresentaciones(30);
        reporte.setImporteTotal(new BigDecimal("1234.56"));
        FilaReporteAdminDTO fila = new FilaReporteAdminDTO();
        fila.setGrupo("Capital");
        fila.setPedidos(8);
        fila.setPresentaciones(20);
        fila.setImporte(new BigDecimal("900.00"));
        reporte.setFilas(List.of(fila));
        reporte.setPorEstado(Map.of("CONFIRMADO", 8L));

        when(servicioReportes.ventas(any(), any(), any(), any())).thenReturn(reporte);
        when(servicioReportes.pedidosPorPreventista(any(), any(), any(), anyBoolean())).thenReturn(List.of());
        when(servicioReportes.stock(any())).thenReturn(List.of(Map.of(
                "producto", "Alfajor", "codigo", "DEMO-001", "cantidadActual", 2400,
                "stockMinimo", 20, "bajoMinimo", false)));

        mvc().perform(get("/reportes").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reportes y estadísticas")))
                .andExpect(content().string(containsString("Capital")))
                .andExpect(content().string(containsString("Alfajor")))
                .andExpect(content().string(containsString("Exportar CSV")));
    }

    @Test
    void lasNotificacionesMuestranCorreosYSuscriptores() throws Exception {
        CorreoAdminDTO correo = new CorreoAdminDTO();
        correo.setDestinatario("kiosco.demo@dulcemayor.test");
        correo.setAsunto("Pedido confirmado — Kiosco Demo");
        correo.setTipo("PEDIDO_CONFIRMADO");
        correo.setEstado("SIMULADO");
        when(servicioNotificaciones.correos(any())).thenReturn(List.of(correo));

        SuscripcionAdminDTO suscripcion = new SuscripcionAdminDTO();
        suscripcion.setEmail("newsletter1@dulcemayor.test");
        suscripcion.setActiva(true);
        when(servicioNotificaciones.suscripciones(any())).thenReturn(List.of(suscripcion));

        mvc().perform(get("/notificaciones").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Correo y newsletter")))
                .andExpect(content().string(containsString("Pedido confirmado")))
                .andExpect(content().string(containsString("newsletter1@dulcemayor.test")))
                .andExpect(content().string(containsString("Enviar newsletter")));
    }

    @Test
    void losPuntosDeVentaListanComerciosConUbicacion() throws Exception {
        PuntoVentaAdminDTO punto = new PuntoVentaAdminDTO();
        punto.setNombreComercial("Kiosco Demo");
        punto.setNombreCliente("Marta");
        punto.setApellidoCliente("Gómez");
        punto.setTelefono("2615551234");
        com.dulcemayor.admin.dto.DireccionDTO direccion = new com.dulcemayor.admin.dto.DireccionDTO();
        direccion.setCalle("Las Heras");
        direccion.setNumeracion("250");
        direccion.setLocalidad("Ciudad de Mendoza");
        punto.setDireccion(direccion);
        when(servicioPuntosVenta.listar(any())).thenReturn(List.of(punto));

        mvc().perform(get("/puntos-venta").session(sesionAdministrativa()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kiosco Demo")))
                .andExpect(content().string(containsString("Marta Gómez")))
                .andExpect(content().string(containsString("Las Heras 250")));
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
}
