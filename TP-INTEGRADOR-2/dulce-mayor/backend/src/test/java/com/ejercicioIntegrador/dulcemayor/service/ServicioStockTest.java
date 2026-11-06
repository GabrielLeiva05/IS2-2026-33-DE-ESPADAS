package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.StockRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.Stock;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioReservaStock;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioStockTest {

    private RepositorioStock repositorio;
    private ServicioStock servicio;
    private Producto alfajor;

    @BeforeEach
    void setUp() throws MiException {
        repositorio = mock(RepositorioStock.class);
        ServicioProducto servicioProducto = mock(ServicioProducto.class);
        servicio = new ServicioStock(repositorio, servicioProducto, mock(RepositorioReservaStock.class));

        alfajor = Datos.producto("prod-1", "ALF-001", "Alfajor triple", null);
        when(servicioProducto.obtenerEntidad("prod-1")).thenReturn(alfajor);
        when(repositorio.save(any(Stock.class))).then(returnsFirstArg());
    }

    @Test
    void altaRegistraElStockInicialConFechaDeActualizacion() throws MiException {
        when(repositorio.findByProducto_IdAndEliminadoFalse("prod-1")).thenReturn(Optional.empty());

        StockResponseDTO creado = servicio.alta(new StockRequestDTO("prod-1", 50, 10));

        assertEquals(50, creado.cantidadActual());
        assertNotNull(creado.fechaActualizacion());
        assertFalse(creado.bajoMinimo());
    }

    @Test
    void altaRechazaUnProductoQueYaTieneStock() {
        when(repositorio.findByProducto_IdAndEliminadoFalse("prod-1"))
                .thenReturn(Optional.of(Datos.stock("s-1", alfajor, 5, 0)));

        assertThrows(MiException.class, () -> servicio.alta(new StockRequestDTO("prod-1", 50, 10)));
        verify(repositorio, never()).save(any(Stock.class));
    }

    @Test
    void descontarRestaLaCantidadYMarcaBajoMinimo() throws MiException {
        Stock stock = Datos.stock("s-1", alfajor, 12, 10);
        when(repositorio.findByProducto_IdAndEliminadoFalse("prod-1")).thenReturn(Optional.of(stock));
        when(repositorio.sumarReservasActivas("prod-1")).thenReturn(0L);
        when(repositorio.buscarParaActualizarPorProducto("prod-1")).thenReturn(Optional.of(stock));

        StockResponseDTO resultado = servicio.descontarStock("prod-1", 5);

        assertEquals(7, resultado.cantidadActual());
        assertTrue(resultado.bajoMinimo());
    }

    @Test
    void descontarRechazaUnaCantidadMayorAlStockDisponible() {
        Stock stock = Datos.stock("s-1", alfajor, 3, 0);
        when(repositorio.buscarParaActualizarPorProducto("prod-1")).thenReturn(Optional.of(stock));

        assertThrows(MiException.class, () -> servicio.descontarStock("prod-1", 5));
        assertEquals(3, stock.getCantidadActual());
        verify(repositorio, never()).save(any(Stock.class));
    }

    @Test
    void reponerSumaLaCantidad() throws MiException {
        Stock stock = Datos.stock("s-1", alfajor, 0, 10);
        when(repositorio.buscarParaActualizarPorProducto("prod-1")).thenReturn(Optional.of(stock));

        assertEquals(30, servicio.reponerStock("prod-1", 30).cantidadActual());
    }

    @Test
    void reponerRechazaCantidadesNoPositivas() {
        assertThrows(MiException.class, () -> servicio.reponerStock("prod-1", 0));
        assertThrows(MiException.class, () -> servicio.reponerStock("prod-1", null));
    }

    @Test
    void hayStockSuficienteComparaConLaCantidadDisponible() throws MiException {
        when(repositorio.findByProducto_IdAndEliminadoFalse("prod-1"))
                .thenReturn(Optional.of(Datos.stock("s-1", alfajor, 4, 0)));

        assertTrue(servicio.hayStockSuficiente("prod-1", 4));
        assertFalse(servicio.hayStockSuficiente("prod-1", 5));
    }

    @Test
    void disponibilidadParaPedidoExcluyeSuReservaYConservaLasDemas() {
        when(repositorio.findByProducto_IdAndEliminadoFalse("prod-1"))
                .thenReturn(Optional.of(Datos.stock("s-1", alfajor, 20, 0)));
        when(repositorio.sumarReservasActivasExceptoPedido("prod-1", "pedido-1")).thenReturn(12L);

        assertEquals(8, servicio.cantidadDisponibleParaPedido("prod-1", "pedido-1"));
    }

    @Test
    void elStockNoSeModificaNiSeDaDeBajaPorLasOperacionesGenericas() {
        when(repositorio.findByIdAndEliminadoFalse("s-1")).thenReturn(Optional.of(Datos.stock("s-1", alfajor, 4, 0)));

        assertThrows(OperacionNoPermitidaException.class,
                () -> servicio.modificar("s-1", new StockRequestDTO("prod-1", 99, 0)));
        assertThrows(OperacionNoPermitidaException.class, () -> servicio.baja("s-1"));
    }
}
