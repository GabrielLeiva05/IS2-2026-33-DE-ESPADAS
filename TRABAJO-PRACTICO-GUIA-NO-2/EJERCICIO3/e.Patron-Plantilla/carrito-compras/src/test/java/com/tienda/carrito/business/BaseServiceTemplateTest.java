package com.tienda.carrito.business;

import com.tienda.carrito.business.domain.entity.BaseEntity;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.BaseService;
import com.tienda.carrito.business.logic.service.BaseUseCaseService;
import com.tienda.carrito.business.percistence.repository.BaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Demuestra que BaseService ejecuta los pasos del Template Method en el orden esperado. */
@ExtendWith(MockitoExtension.class)
class BaseServiceTemplateTest {

    static class Dummy extends BaseEntity<String> {
        private String id;
        private String valor;

        @Override public String getId() { return id; }
        @Override public void setId(String id) { this.id = id; }
        @Override public Boolean isEliminado() { return eliminado; }
        @Override public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
        String getValor() { return valor; }
        void setValor(String valor) { this.valor = valor; }
    }

    /** Servicio de prueba: registra en una lista el orden en que se ejecutan los pasos. */
    static class DummyService extends BaseService<Dummy, String> {
        final List<String> pasos = new ArrayList<>();

        DummyService(BaseRepository<Dummy, String> repo) { super(repo); }

        @Override
        protected void validar(BaseUseCaseService useCase, Dummy d) throws ErrorServiceException {
            pasos.add("validar:" + useCase);
            if (useCase != BaseUseCaseService.BAJA && d.getValor() == null) {
                throw new ErrorServiceException("valor obligatorio");
            }
        }

        @Override protected void preAlta(Dummy d) { pasos.add("preAlta"); }

        @Override protected void postAlta(Dummy d) { pasos.add("postAlta"); }

        @Override protected void preModificacion(Dummy d) { pasos.add("preModificacion"); }

        @Override protected void preBaja(Dummy d) { pasos.add("preBaja"); }
    }

    @Mock
    BaseRepository<Dummy, String> repositorio;

    @Test
    void alta_ejecutaLosPasosEnOrden_eIgnoraElIdRecibido() throws Exception {
        DummyService servicio = new DummyService(repositorio);
        when(repositorio.save(any(Dummy.class))).thenAnswer(inv -> inv.getArgument(0));
        Dummy d = new Dummy();
        d.setValor("x");
        d.setId("id-que-debe-ignorarse");

        Dummy creado = servicio.alta(d);

        assertEquals(List.of("validar:ALTA", "preAlta", "postAlta"), servicio.pasos);
        assertNull(creado.getId());
    }

    @Test
    void alta_siFallaLaValidacion_noGuarda() {
        DummyService servicio = new DummyService(repositorio);

        assertThrows(ErrorServiceException.class, () -> servicio.alta(new Dummy()));

        verify(repositorio, never()).save(any(Dummy.class));
        assertEquals(List.of("validar:ALTA"), servicio.pasos);
    }

    @Test
    void modificar_ejecutaLosPasosEnOrden() throws Exception {
        DummyService servicio = new DummyService(repositorio);
        Dummy existente = new Dummy();
        existente.setId("1");
        when(repositorio.findById("1")).thenReturn(Optional.of(existente));
        when(repositorio.save(any(Dummy.class))).thenAnswer(inv -> inv.getArgument(0));
        Dummy nuevo = new Dummy();
        nuevo.setValor("nuevo");

        Optional<Dummy> resultado = servicio.modificar("1", nuevo);

        assertEquals(List.of("validar:MODIFICACION", "preModificacion"), servicio.pasos);
        assertTrue(resultado.isPresent());
        assertEquals("1", resultado.get().getId());
    }

    @Test
    void modificar_noTocaRegistrosYaEliminados() throws Exception {
        DummyService servicio = new DummyService(repositorio);
        Dummy eliminado = new Dummy();
        eliminado.setId("1");
        eliminado.setEliminado(true);
        when(repositorio.findById("1")).thenReturn(Optional.of(eliminado));
        Dummy nuevo = new Dummy();
        nuevo.setValor("x");

        assertTrue(servicio.modificar("1", nuevo).isEmpty());

        verify(repositorio, never()).save(any(Dummy.class));
    }

    @Test
    void baja_ejecutaPreBaja_yHaceBajaLogica() throws Exception {
        DummyService servicio = new DummyService(repositorio);
        Dummy existente = new Dummy();
        existente.setId("1");
        when(repositorio.findById("1")).thenReturn(Optional.of(existente));

        servicio.baja("1");

        assertEquals(List.of("validar:BAJA", "preBaja"), servicio.pasos);
        assertTrue(existente.isEliminado());
        verify(repositorio).save(existente);
    }
}
