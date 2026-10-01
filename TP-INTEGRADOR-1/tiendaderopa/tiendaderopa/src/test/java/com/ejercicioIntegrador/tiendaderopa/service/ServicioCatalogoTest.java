package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.dto.ProductoCatalogoDTO;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServicioCatalogoTest {

    @Test
    void incluyeProductoActivoAunqueAunNoTengaPrecioNiStock() {
        ServicioProducto productos = mock(ServicioProducto.class);
        ServicioVigenciaPrecio precios = mock(ServicioVigenciaPrecio.class);
        ServicioStock stock = mock(ServicioStock.class);
        Producto producto = new Producto();
        producto.setId("producto-nuevo");
        producto.setEliminado(false);
        when(productos.listarProductoActivo()).thenReturn(List.of(producto));
        when(precios.buscarVigenciaPrecioVigente("producto-nuevo")).thenReturn(null);

        ServicioCatalogo catalogo = new ServicioCatalogo(productos, precios, stock);

        assertEquals(List.of(new ProductoCatalogoDTO(producto, 0, 0)), catalogo.listarDisponibles());
    }
}