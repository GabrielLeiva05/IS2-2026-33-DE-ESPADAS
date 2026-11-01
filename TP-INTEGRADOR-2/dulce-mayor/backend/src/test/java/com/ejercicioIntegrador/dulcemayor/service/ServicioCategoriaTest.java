package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.CategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioCategoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class ServicioCategoriaTest {

    private RepositorioCategoria repositorio;
    private ServicioCategoria servicio;

    @BeforeEach
    void setUp() {
        repositorio = mock(RepositorioCategoria.class);
        servicio = new ServicioCategoria(repositorio);
        when(repositorio.save(any(Categoria.class))).then(returnsFirstArg());
        when(repositorio.findFirstByNombreIgnoreCaseAndEliminadoFalse(any())).thenReturn(Optional.empty());
    }

    @Test
    void altaGuardaLaCategoriaConElNombreSinEspacios() throws MiException {
        CategoriaResponseDTO creada = servicio.alta(new CategoriaRequestDTO("  Chocolates  "));

        assertEquals("Chocolates", creada.nombre());
        verify(repositorio).save(any(Categoria.class));
    }

    @Test
    void altaRechazaUnNombreVacio() {
        MiException error = assertThrows(MiException.class, () -> servicio.alta(new CategoriaRequestDTO("   ")));

        assertEquals("Debe indicar el nombre de la categoría", error.getMessage());
        verify(repositorio, never()).save(any(Categoria.class));
    }

    @Test
    void altaRechazaUnNombreRepetido() {
        when(repositorio.findFirstByNombreIgnoreCaseAndEliminadoFalse("Alfajores"))
                .thenReturn(Optional.of(Datos.categoria("cat-1", "Alfajores")));

        assertThrows(MiException.class, () -> servicio.alta(new CategoriaRequestDTO("Alfajores")));
        verify(repositorio, never()).save(any(Categoria.class));
    }

    @Test
    void modificarPermiteConservarElMismoNombre() throws MiException {
        Categoria existente = Datos.categoria("cat-1", "Alfajores");
        when(repositorio.findByIdAndEliminadoFalse("cat-1")).thenReturn(Optional.of(existente));
        when(repositorio.findFirstByNombreIgnoreCaseAndEliminadoFalse("Alfajores")).thenReturn(Optional.of(existente));

        CategoriaResponseDTO modificada = servicio.modificar("cat-1", new CategoriaRequestDTO("Alfajores"));

        assertEquals("cat-1", modificada.id());
    }

    @Test
    void bajaMarcaLaCategoriaComoEliminada() throws MiException {
        Categoria existente = Datos.categoria("cat-1", "Alfajores");
        when(repositorio.findByIdAndEliminadoFalse("cat-1")).thenReturn(Optional.of(existente));

        servicio.baja("cat-1");

        assertTrue(existente.isEliminado());
        verify(repositorio).save(existente);
    }

    @Test
    void consultarUnaCategoriaInexistenteInformaRecursoNoEncontrado() {
        when(repositorio.findByIdAndEliminadoFalse("no-existe")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.consultar("no-existe"));
    }
}
