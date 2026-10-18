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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    // ---------- Ubicacion (OpenStreetMap) ----------

    private Proveedor conUbicacion(String nombre, Double latitud, Double longitud) {
        Proveedor p = proveedor(null, nombre);
        p.setLatitud(latitud);
        p.setLongitud(longitud);
        return p;
    }

    @Test
    void alta_conSoloLatitud_lanzaError() {
        assertThrows(ErrorServiceException.class, () -> servicio.alta(conUbicacion("Andina", -32.88, null)));
        verify(repositorio, never()).save(any(Proveedor.class));
    }

    @Test
    void alta_conLatitudFueraDeRango_lanzaError() {
        assertThrows(ErrorServiceException.class, () -> servicio.alta(conUbicacion("Andina", 95.0, -68.84)));
        verify(repositorio, never()).save(any(Proveedor.class));
    }

    @Test
    void alta_conLongitudFueraDeRango_lanzaError() {
        assertThrows(ErrorServiceException.class, () -> servicio.alta(conUbicacion("Andina", -32.88, 200.0)));
        verify(repositorio, never()).save(any(Proveedor.class));
    }

    @Test
    void alta_conDireccionMuyLarga_lanzaError() {
        Proveedor p = proveedor(null, "Andina");
        p.setDireccion("x".repeat(256));
        assertThrows(ErrorServiceException.class, () -> servicio.alta(p));
        verify(repositorio, never()).save(any(Proveedor.class));
    }

    @Test
    void alta_conUbicacionValida_guardaDireccionRecortadaYCoordenadas() throws Exception {
        when(repositorio.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
        Proveedor p = conUbicacion("Andina", -32.8947, -68.8433);
        p.setDireccion("  Av. San Martín 1200  ");

        Proveedor creado = servicio.alta(p);

        assertEquals("Av. San Martín 1200", creado.getDireccion());
        assertEquals(-32.8947, creado.getLatitud().doubleValue());
        assertEquals(-68.8433, creado.getLongitud().doubleValue());
    }

    @Test
    void alta_sinUbicacion_esValida() throws Exception {
        when(repositorio.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        Proveedor creado = servicio.alta(proveedor(null, "Andina"));

        assertNull(creado.getLatitud());
        assertNull(creado.getLongitud());
    }

    @Test
    void listarConUbicacion_excluyeLosQueNoTienenCoordenadas() throws Exception {
        Proveedor con = conUbicacion("Con", -32.88, -68.84);
        Proveedor sin = proveedor("2", "Sin");
        Proveedor soloLat = conUbicacion("SoloLat", -32.88, null);
        when(repositorio.findAll()).thenReturn(List.of(con, sin, soloLat));

        List<Proveedor> resultado = servicio.listarConUbicacion();

        assertEquals(1, resultado.size());
        assertEquals("Con", resultado.get(0).getNombre());
    }
}
