package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioOrdenCompraProveedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioOrdenCompraProveedorTest {

    private ServicioOrdenCompraProveedor servicio;
    private RepositorioOrdenCompraProveedor repositorio;
    private ServicioStock servicioStock;

    @BeforeEach
    void setUp() {
        servicio = new ServicioOrdenCompraProveedor();
        repositorio = mock(RepositorioOrdenCompraProveedor.class);
        servicioStock = mock(ServicioStock.class);
        ReflectionTestUtils.setField(servicio, "repositorio", repositorio);
        ReflectionTestUtils.setField(servicio, "servicioStock", servicioStock);
    }

    @Test
    void recibirOrdenSumaStockUnaSolaVez() throws Exception {
        OrdenCompraProveedor orden = new OrdenCompraProveedor();
        orden.setId("orden-proveedor-1");
        orden.setEstado(EstadoOrdenCompraProveedor.PENDIENTE);
        when(repositorio.findById(orden.getId())).thenReturn(Optional.of(orden));

        servicio.marcarComoEntregada(orden.getId());
        assertEquals(EstadoOrdenCompraProveedor.ENTREGADA, orden.getEstado());
        assertThrows(Exception.class, () -> servicio.marcarComoEntregada(orden.getId()));

        verify(servicioStock, times(1)).registrarRecepcion(orden);
    }
}