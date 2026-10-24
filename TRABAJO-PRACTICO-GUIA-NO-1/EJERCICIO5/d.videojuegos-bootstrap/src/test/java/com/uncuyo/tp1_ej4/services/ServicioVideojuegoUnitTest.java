package com.uncuyo.tp1_ej4.services;

import com.uncuyo.tp1_ej4.entities.Videojuego;
import com.uncuyo.tp1_ej4.repositories.RepositorioVideojuego;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioVideojuegoUnitTest {

    @Mock
    private RepositorioVideojuego repositorio;

    @InjectMocks
    private ServicioVideojuego servicio;

    @Test
    void findAllByActivo_delegaEnRepositorio() throws Exception {
        List<Videojuego> esperados = List.of(new Videojuego());
        when(repositorio.findAllByActivo()).thenReturn(esperados);

        List<Videojuego> resultado = servicio.findAllByActivo();

        assertSame(esperados, resultado);
        verify(repositorio).findAllByActivo();
    }

    @Test
    void deleteById_invierteActivoYGuardaLaEntidad() throws Exception {
        Videojuego videojuego = new Videojuego();
        videojuego.setActivo(true);
        when(repositorio.findById(7L)).thenReturn(Optional.of(videojuego));

        boolean resultado = servicio.deleteById(7L);

        assertTrue(resultado);
        assertFalse(videojuego.isActivo());
        verify(repositorio).save(videojuego);
    }

    @Test
    void deleteById_noGuardaSiElIdNoExiste() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> servicio.deleteById(999L));

        verify(repositorio, never()).save(org.mockito.ArgumentMatchers.any(Videojuego.class));
    }
}
