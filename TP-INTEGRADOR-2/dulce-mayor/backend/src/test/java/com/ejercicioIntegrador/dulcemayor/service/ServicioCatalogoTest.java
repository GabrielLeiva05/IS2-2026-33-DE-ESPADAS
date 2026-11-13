package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.FiltroCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.service.cotizacion.ServicioCotizacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioCatalogoTest {

    private ServicioProducto servicioProducto;
    private ServicioVigenciaPrecio servicioVigenciaPrecio;
    private ServicioStock servicioStock;
    private ServicioCotizacion servicioCotizacion;
    private ServicioPromocion servicioPromocion; // <-- Agregado
    private ServicioCatalogo servicio;
    private SubCategoria alfajores;

    @BeforeEach
    void setUp() {
        servicioProducto = mock(ServicioProducto.class);
        servicioVigenciaPrecio = mock(ServicioVigenciaPrecio.class);
        servicioStock = mock(ServicioStock.class);
        servicioCotizacion = mock(ServicioCotizacion.class);
        servicioPromocion = mock(ServicioPromocion.class); // <-- Mockeado

        // Se pasan las 5 dependencias al constructor
        servicio = new ServicioCatalogo(servicioProducto, servicioVigenciaPrecio, servicioStock, servicioCotizacion, servicioPromocion);

        alfajores = Datos.subCategoria("sub-1", "Alfajores", Datos.categoria("cat-1", "Golosinas"));
    }

    private Producto disponible(String id, String nombre, int stock, double precio) {
        Producto producto = Datos.producto(id, id.toUpperCase(), nombre, alfajores);
        when(servicioStock.cantidadDisponible(id)).thenReturn(stock);
        when(servicioVigenciaPrecio.obtenerVigenciaVigente(id))
                .thenReturn(Optional.of(Datos.vigencia("v-" + id, producto, LocalDate.now(), null, precio)));
        return producto;
    }

    @Test
    void muestraSoloProductosConStockYPrecioVigente() {
        Producto conStock = disponible("p1", "Alfajor triple", 10, 18000);
        Producto sinStock = disponible("p2", "Alfajor simple", 0, 9000);
        Producto sinPrecio = disponible("p3", "Alfajor de maicena", 5, 0);
        when(servicioVigenciaPrecio.obtenerVigenciaVigente("p3")).thenReturn(Optional.empty());
        when(servicioProducto.listarEntidadesActivas()).thenReturn(List.of(conStock, sinStock, sinPrecio));
        when(servicioPromocion.automaticasVigentes()).thenReturn(List.of()); // <-- Evita NullPointerException al consultar promociones

        List<ProductoCatalogoDTO> catalogo = servicio.listarProductoConStock(null);

        assertEquals(1, catalogo.size());
        assertEquals("p1", catalogo.get(0).id());
        assertEquals(18000, catalogo.get(0).precio());
        assertEquals(10, catalogo.get(0).stockDisponible());
    }

    @Test
    void ocultaLosProductosDeUnaCategoriaDadaDeBaja() {
        Categoria eliminada = Datos.categoria("cat-2", "Discontinuados");
        eliminada.setEliminado(true);
        Producto producto = disponible("p1", "Alfajor triple", 10, 18000);
        producto.setSubCategoria(Datos.subCategoria("sub-2", "Viejos", eliminada));
        when(servicioProducto.listarEntidadesActivas()).thenReturn(List.of(producto));
        when(servicioPromocion.automaticasVigentes()).thenReturn(List.of());

        assertTrue(servicio.listarProductoConStock(null).isEmpty());
    }

    @Test
    void aplicaLosFiltrosDeTextoYOfertaYOrdenaPorNombre() {
        Producto triple = disponible("p1", "Alfajor triple", 10, 18000);
        Producto chocolate = disponible("p2", "Alfajor de chocolate", 10, 15000);
        Producto chupetin = disponible("p3", "Chupetín", 10, 5000);
        triple.setEnOferta(true);
        chocolate.setEnOferta(true);
        when(servicioProducto.listarEntidadesActivas()).thenReturn(List.of(triple, chocolate, chupetin));
        when(servicioPromocion.automaticasVigentes()).thenReturn(List.of());

        List<ProductoCatalogoDTO> catalogo = servicio.listarProductoConStock(
                new FiltroCatalogoDTO(null, null, "alfajor", true));

        assertEquals(List.of("Alfajor de chocolate", "Alfajor triple"),
                catalogo.stream().map(ProductoCatalogoDTO::nombre).toList());
    }

    @Test
    void consultarUnProductoSinStockInformaQueNoEstaDisponible() throws MiException {
        Producto sinStock = disponible("p1", "Alfajor triple", 0, 18000);
        when(servicioProducto.obtenerEntidad("p1")).thenReturn(sinStock);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.consultarProductoDisponible("p1"));
    }

    @Test
    void conCotizacionMuestraElPrecioEnDolaresConsultandolaUnaSolaVezPorListado() {
        Producto a = disponible("p1", "Alfajor triple", 10, 18000);
        Producto b = disponible("p2", "Chupetín", 10, 1000);
        when(servicioProducto.listarEntidadesActivas()).thenReturn(List.of(a, b));
        when(servicioCotizacion.ventaVigente()).thenReturn(Optional.of(new BigDecimal("1200.00")));
        when(servicioPromocion.automaticasVigentes()).thenReturn(List.of());

        List<ProductoCatalogoDTO> catalogo = servicio.listarProductoConStock(null);

        assertEquals(new BigDecimal("15.00"), catalogo.get(0).precioUsd());   // 18000 / 1200
        assertEquals(new BigDecimal("0.83"), catalogo.get(1).precioUsd());    // 1000 / 1200 = 0,8333…
        verify(servicioCotizacion, times(1)).ventaVigente();
    }

    @Test
    void sinCotizacionElPrecioEnDolaresQuedaVacioYElCatalogoFuncionaIgual() {
        Producto a = disponible("p1", "Alfajor triple", 10, 18000);
        when(servicioProducto.listarEntidadesActivas()).thenReturn(List.of(a));
        when(servicioCotizacion.ventaVigente()).thenReturn(Optional.empty());
        when(servicioPromocion.automaticasVigentes()).thenReturn(List.of());

        List<ProductoCatalogoDTO> catalogo = servicio.listarProductoConStock(null);

        assertEquals(1, catalogo.size());
        assertNull(catalogo.get(0).precioUsd());
    }
}