package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioProducto;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioProductoTest {

    @Test
    void restaurarProductoQuitaLaBajaLogica() throws Exception {
        RepositorioProducto repositorio = mock(RepositorioProducto.class);
        ServicioProducto servicio = new ServicioProducto();
        ReflectionTestUtils.setField(servicio, "repositorio", repositorio);
        Producto producto = new Producto();
        producto.setId("producto-1");
        producto.setEliminado(true);
        when(repositorio.findById("producto-1")).thenReturn(Optional.of(producto));
        when(repositorio.save(producto)).thenReturn(producto);

        servicio.restaurarProducto("producto-1");

        assertFalse(producto.isEliminado());
        verify(repositorio).save(producto);
    }
}