package com.tienda.carrito.business;

import com.tienda.carrito.business.domain.entity.Proveedor;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.ProveedorService;
import com.tienda.carrito.business.percistence.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    ProveedorRepository repositorio;

    ProveedorService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ProveedorService(repositorio);
    }

    private Proveedor proveedor(String id, String nombre) {
        Proveedor p = new Proveedor();
        p.setId(id);
        p.setNombre(nombre);
        return p;
    }

    @Test
    void alta_conNombreVacio_lanzaError() {
        assertThrows(ErrorServiceException.class, () -> servicio.alta(proveedor(null, "   ")));
        verify(repositorio, never()).save(any(Proveedor.class));
    }

    @Test
    void alta_conNombreDuplicado_lanzaError() {
        when(repositorio.buscarProveedorPorNombre("Andina")).thenReturn(proveedor("otro", "Andina"));

        assertThrows(ErrorServiceException.class, () -> servicio.alta(proveedor(null, "Andina")));

        verify(repositorio, never()).save(any(Proveedor.class));
    }

    @Test
    void alta_correcta_guardaConNombreRecortado() throws Exception {
        when(repositorio.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        Proveedor creado = servicio.alta(proveedor(null, "  Andina  "));

        assertEquals("Andina", creado.getNombre());
        assertFalse(creado.isEliminado());
    }

    @Test
    void modificar_conSuPropioNombre_noEsDuplicado() throws Exception {
        Proveedor existente = proveedor("1", "Andina");
        when(repositorio.buscarProveedorPorNombre("Andina")).thenReturn(existente);
        when(repositorio.findById("1")).thenReturn(Optional.of(existente));
        when(repositorio.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Proveedor> resultado = servicio.modificar("1", proveedor(null, "Andina"));

        assertTrue(resultado.isPresent());
    }
}
