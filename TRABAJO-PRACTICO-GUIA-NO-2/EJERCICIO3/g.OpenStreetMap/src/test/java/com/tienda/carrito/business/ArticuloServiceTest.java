package com.tienda.carrito.business;

import com.tienda.carrito.business.domain.entity.Articulo;
import com.tienda.carrito.business.domain.entity.Imagen;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.ArticuloService;
import com.tienda.carrito.business.logic.service.ImagenService;
import com.tienda.carrito.business.logic.service.ProveedorService;
import com.tienda.carrito.business.percistence.repository.ArticuloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticuloServiceTest {

    @Mock
    ArticuloRepository repositorio;
    @Mock
    ProveedorService proveedorService;
    @Mock
    ImagenService imagenService;

    ArticuloService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ArticuloService(repositorio, proveedorService, imagenService);
    }

    @Test
    void altaConImagenes_sinImagenes_lanzaError_yNoGuardaNada() {
        assertThrows(ErrorServiceException.class, () -> servicio.altaConImagenes(new Articulo(), List.of()));
        verify(repositorio, never()).save(any(Articulo.class));
    }

    @Test
    void quitarImagen_laUltima_lanzaError() throws Exception {
        Imagen unica = new Imagen();
        unica.setId("i1");
        Articulo articulo = new Articulo();
        articulo.setId("a1");
        articulo.getImagenes().add(unica);
        when(repositorio.findById("a1")).thenReturn(Optional.of(articulo));

        assertThrows(ErrorServiceException.class, () -> servicio.quitarImagen("a1", "i1"));

        verify(imagenService, never()).baja(anyString());
    }
}
