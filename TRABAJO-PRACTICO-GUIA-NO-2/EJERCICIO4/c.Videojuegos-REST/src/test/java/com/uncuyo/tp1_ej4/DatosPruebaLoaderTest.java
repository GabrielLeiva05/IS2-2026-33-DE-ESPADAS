package com.uncuyo.tp1_ej4;

import com.uncuyo.tp1_ej4.entities.Categoria;
import com.uncuyo.tp1_ej4.entities.Estudio;
import com.uncuyo.tp1_ej4.entities.Videojuego;
import com.uncuyo.tp1_ej4.repositories.RepositorioCategoria;
import com.uncuyo.tp1_ej4.repositories.RepositorioEstudio;
import com.uncuyo.tp1_ej4.repositories.RepositorioVideojuego;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosPruebaLoaderTest {

    @Mock
    private RepositorioCategoria repositorioCategoria;
    @Mock
    private RepositorioEstudio repositorioEstudio;
    @Mock
    private RepositorioVideojuego repositorioVideojuego;

    @InjectMocks
    private DatosPruebaLoader loader;

    @Test
    void cargaDatosDePruebaCuandoLasTablasEstanVacias() {
        when(repositorioCategoria.count()).thenReturn(0L);
        when(repositorioEstudio.count()).thenReturn(0L);
        when(repositorioVideojuego.count()).thenReturn(0L);
        when(repositorioCategoria.saveAll(anyList())).thenAnswer(invocation -> argumento(invocation));
        when(repositorioEstudio.saveAll(anyList())).thenAnswer(invocation -> argumento(invocation));

        AtomicReference<List<Videojuego>> videojuegosGuardados = new AtomicReference<>();
        doAnswer(invocation -> {
            List<Videojuego> videojuegos = argumento(invocation);
            videojuegosGuardados.set(new ArrayList<>(videojuegos));
            return videojuegos;
        }).when(repositorioVideojuego).saveAll(anyList());

        loader.run(new DefaultApplicationArguments());

        verify(repositorioCategoria).saveAll(anyList());
        verify(repositorioEstudio).saveAll(anyList());
        verify(repositorioVideojuego).saveAll(anyList());
        assertEquals(3, videojuegosGuardados.get().size());
        assertTrue(videojuegosGuardados.get().stream().allMatch(Videojuego::isActivo));
        assertTrue(videojuegosGuardados.get().stream()
                .allMatch(videojuego -> videojuego.getCategoria() != null
                        && videojuego.getEstudio() != null));
    }

    @Test
    void noAgregaDatosSiLaBaseYaTieneRegistros() {
        when(repositorioCategoria.count()).thenReturn(1L);

        loader.run(new DefaultApplicationArguments());

        verify(repositorioCategoria, never()).saveAll(anyList());
        verify(repositorioEstudio, never()).saveAll(anyList());
        verify(repositorioVideojuego, never()).saveAll(anyList());
    }

    private static <T> List<T> argumento(InvocationOnMock invocation) {
        List<T> entidades = invocation.getArgument(0);
        return entidades;
    }
}
