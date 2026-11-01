package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.VigenciaPrecio;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioVigenciaPrecio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioVigenciaPrecioTest {

    private static final LocalDate HOY = LocalDate.now();

    private RepositorioVigenciaPrecio repositorio;
    private ServicioVigenciaPrecio servicio;
    private Producto alfajor;

    @BeforeEach
    void setUp() throws MiException {
        repositorio = mock(RepositorioVigenciaPrecio.class);
        ServicioProducto servicioProducto = mock(ServicioProducto.class);
        servicio = new ServicioVigenciaPrecio(repositorio, servicioProducto);

        alfajor = Datos.producto("prod-1", "ALF-001", "Alfajor triple", null);
        when(servicioProducto.obtenerEntidad("prod-1")).thenReturn(alfajor);
        when(repositorio.save(any(VigenciaPrecio.class))).then(returnsFirstArg());
    }

    @Test
    void altaCierraLaVigenciaAbiertaElDiaAnteriorAlInicioDeLaNueva() throws MiException {
        VigenciaPrecio abierta = Datos.vigencia("v-1", alfajor, HOY.minusMonths(3), null, 16500);
        when(repositorio.findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc("prod-1")).thenReturn(List.of(abierta));

        servicio.alta(new VigenciaPrecioRequestDTO(HOY, null, 18000.0, "prod-1"));

        assertEquals(HOY.minusDays(1), abierta.getFechaHasta());
        verify(repositorio).save(abierta);
    }

    @Test
    void altaRechazaUnaVigenciaQueNoEmpiezaDespuesDeLaActual() {
        VigenciaPrecio abierta = Datos.vigencia("v-1", alfajor, HOY, null, 16500);
        when(repositorio.findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc("prod-1")).thenReturn(List.of(abierta));

        assertThrows(MiException.class,
                () -> servicio.alta(new VigenciaPrecioRequestDTO(HOY.minusDays(5), null, 18000.0, "prod-1")));
        verify(repositorio, never()).save(any(VigenciaPrecio.class));
    }

    @Test
    void altaRechazaUnPrecioNoPositivo() {
        when(repositorio.findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc("prod-1")).thenReturn(List.of());

        MiException error = assertThrows(MiException.class,
                () -> servicio.alta(new VigenciaPrecioRequestDTO(HOY, null, 0.0, "prod-1")));

        assertEquals("El precio debe ser mayor a cero", error.getMessage());
    }

    @Test
    void modificarRechazaUnPeriodoQueSeSuperponeConOtraVigencia() {
        VigenciaPrecio anterior = Datos.vigencia("v-1", alfajor, HOY.minusMonths(2), HOY.minusMonths(1), 15000);
        VigenciaPrecio actual = Datos.vigencia("v-2", alfajor, HOY.minusMonths(1).plusDays(1), null, 16500);
        when(repositorio.findByIdAndEliminadoFalse("v-2")).thenReturn(Optional.of(actual));
        when(repositorio.findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc("prod-1"))
                .thenReturn(List.of(actual, anterior));

        assertThrows(MiException.class, () -> servicio.modificar("v-2",
                new VigenciaPrecioRequestDTO(HOY.minusMonths(1).minusDays(10), null, 16500.0, "prod-1")));
    }

    @Test
    void elPrecioVigenteEsElQueCubreLaFechaDeHoy() {
        VigenciaPrecio vieja = Datos.vigencia("v-1", alfajor, HOY.minusMonths(3), HOY.minusDays(1), 16500);
        VigenciaPrecio actual = Datos.vigencia("v-2", alfajor, HOY, HOY.plusDays(9), 18000);
        VigenciaPrecio futura = Datos.vigencia("v-3", alfajor, HOY.plusDays(10), null, 19500);
        when(repositorio.findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc("prod-1"))
                .thenReturn(List.of(futura, actual, vieja));

        Optional<VigenciaPrecio> vigente = servicio.obtenerVigenciaVigente("prod-1");

        assertTrue(vigente.isPresent());
        assertEquals(18000, vigente.get().getPrecio());
    }
}
