package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioOrdenCompra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class ServicioOrdenCompraTest {

    private RepositorioOrdenCompra repositorio;
    private ServicioOrdenCompra servicio;
    private ServicioStock servicioStock;
    private ServicioSucursal servicioSucursal;

    @BeforeEach
    void setUp() {
        repositorio = mock(RepositorioOrdenCompra.class);
        servicioStock = mock(ServicioStock.class);
        servicioSucursal = mock(ServicioSucursal.class);
        servicio = new ServicioOrdenCompra();
        ReflectionTestUtils.setField(servicio, "repositorioOrdenCompra", repositorio);
        ReflectionTestUtils.setField(servicio, "servicioStock", servicioStock);
        ReflectionTestUtils.setField(servicio, "servicioSucursal", servicioSucursal);
        Sucursal sucursal = new Sucursal();
        sucursal.setId("sucursal-1");
        when(servicioSucursal.obtenerPrincipal()).thenReturn(sucursal);
    }

    @Test
    void clienteNoPuedeConsultarOrdenAjena() {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        assertThrows(AccessDeniedException.class,
                () -> servicio.buscarAccesibleParaUsuario("orden-1", "cliente-b@example.com", false));
    }

    @Test
    void clientePuedeConsultarSuOrden() throws Exception {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        assertSame(orden, servicio.buscarAccesibleParaUsuario("orden-1", "cliente-a@example.com", false));
    }

    @Test
    void crearOrdenAsociaElUsuarioAutenticado() throws Exception {
        UsuarioServicio usuarios = mock(UsuarioServicio.class);
        Usuario usuario = new Usuario("cliente-a@example.com", "hash", RolUsuario.CLIENTE);
        when(usuarios.buscarActivoPorNombreUsuario("cliente-a@example.com")).thenReturn(usuario);
        when(repositorio.save(any(OrdenCompra.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReflectionTestUtils.setField(servicio, "usuarioServicio", usuarios);

        OrdenCompra creada = servicio.crearOrdenCompra("cliente-a@example.com");

        assertSame(usuario, creada.getUsuario());
        assertEquals("sucursal-1", creada.getSucursal().getId());
        assertEquals(EstadoOrdenCompra.PENDIENTE_DE_PAGO, creada.getEstadoOrdenCompra());
    }

    @Test
    void ordenSoloAvanzaPorLaSecuenciaDefinida() {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        assertThrows(IllegalStateException.class,
                () -> servicio.cambiarEstado("orden-1", EstadoOrdenCompra.ENTREGADO));
        verify(repositorio, never()).save(any());

        servicio.cambiarEstado("orden-1", EstadoOrdenCompra.PAGO_REALIZADO);
        assertEquals(EstadoOrdenCompra.PAGO_REALIZADO, orden.getEstadoOrdenCompra());
        verify(servicioStock).registrarVenta(orden);
    }

    @Test
    void pagoAprobadoDescuentaStockUnaSolaVezAunqueSeRepitaLaNotificacion() throws Exception {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        orden.setId("orden-1");
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        servicio.registrarResultadoMercadoPago("orden-1", "pago-1", "approved");
        servicio.registrarResultadoMercadoPago("orden-1", "pago-1", "approved");

        assertEquals(EstadoOrdenCompra.PAGO_REALIZADO, orden.getEstadoOrdenCompra());
        verify(servicioStock, times(1)).registrarVenta(orden);
    }

    private OrdenCompra ordenDe(String email, EstadoOrdenCompra estado) {
        OrdenCompra orden = new OrdenCompra();
        orden.setId("orden-1");
        orden.setUsuario(new Usuario(email, "hash", RolUsuario.CLIENTE));
        orden.setEstadoOrdenCompra(estado);
        return orden;
    }
}