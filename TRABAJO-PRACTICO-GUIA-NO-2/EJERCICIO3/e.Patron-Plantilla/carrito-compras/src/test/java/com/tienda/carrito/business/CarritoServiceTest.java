package com.tienda.carrito.business;

import com.tienda.carrito.business.domain.entity.Articulo;
import com.tienda.carrito.business.domain.entity.Carrito;
import com.tienda.carrito.business.domain.entity.Detalle;
import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.CarritoService;
import com.tienda.carrito.business.logic.service.DetalleService;
import com.tienda.carrito.business.logic.service.UsuarioService;
import com.tienda.carrito.business.percistence.repository.CarritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas del carrito aislando las dependencias con mocks (el servicio solo habla con otros SERVICIOS). */
@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    CarritoRepository carritoRepository;
    @Mock
    UsuarioService usuarioService;
    @Mock
    DetalleService detalleService;

    CarritoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new CarritoService(carritoRepository, usuarioService, detalleService);
    }

    private Detalle detalle(String id, double precio) {
        Articulo articulo = new Articulo();
        articulo.setPrecio(precio);
        Detalle d = new Detalle();
        d.setId(id);
        d.setArticulo(articulo);
        return d;
    }

    @Test
    void agregarArticulo_recalculaElTotal() throws Exception {
        Carrito carrito = new Carrito();
        carrito.setId("c1");
        carrito.setUsuario(new Usuario());
        when(carritoRepository.buscarCarritoActivoPorUsuario("u1")).thenReturn(carrito);
        when(detalleService.altaParaCarrito(any(Carrito.class), eq("a1"))).thenReturn(detalle("d1", 100.5));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito resultado = servicio.agregarArticulo("u1", "a1");

        assertEquals(100.5, resultado.getTotal(), 0.001);
        assertEquals(1, resultado.getDetallesActivos().size());
    }

    @Test
    void confirmarCompra_conCarritoVacio_lanzaError() {
        when(carritoRepository.buscarCarritoActivoPorUsuario("u1")).thenReturn(new Carrito());

        assertThrows(ErrorServiceException.class, () -> servicio.confirmarCompra("u1"));
    }

    @Test
    void quitarDetalle_sinCarrito_lanzaError() {
        when(carritoRepository.buscarCarritoActivoPorUsuario("u1")).thenReturn(null);

        assertThrows(ErrorServiceException.class, () -> servicio.quitarDetalle("u1", "d1"));
    }

    @Test
    void quitarDetalle_deOtroCarrito_lanzaError_yNoDaDeBajaNada() throws Exception {
        Carrito carrito = new Carrito();
        carrito.getDetalles().add(detalle("d1", 10));
        when(carritoRepository.buscarCarritoActivoPorUsuario("u1")).thenReturn(carrito);

        // "d99" pertenece al carrito de otro usuario
        assertThrows(ErrorServiceException.class, () -> servicio.quitarDetalle("u1", "d99"));

        verify(detalleService, never()).baja(anyString());
    }
}
