package com.uncuyo.tp1_ej4.services;

import com.uncuyo.tp1_ej4.entities.EntidadBase;
import com.uncuyo.tp1_ej4.repositories.RepositorioBase;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Implementación del patrón Template Method para las operaciones CRUD.
 * Los métodos públicos fijan el algoritmo; cada servicio concreto aporta su
 * repositorio y los pasos que dependen de la entidad.
 */
public abstract class ServicioBase<E extends EntidadBase> {

    protected abstract RepositorioBase<E> getRepositorio();

    @Transactional(readOnly = true)
    public final List<E> findAll() {
        return getRepositorio().findAll();
    }

    @Transactional(readOnly = true)
    public final E findById(long id) {
        return getRepositorio().findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un registro con id " + id));
    }

    @Transactional
    public final E saveOne(E entidad) {
        return getRepositorio().save(entidad);
    }

    @Transactional
    public final E updateOne(E entidad, long id) {
        // El ID de la URL es la fuente de verdad; primero se comprueba que el recurso exista.
        if (!getRepositorio().existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No existe un registro con id " + id);
        }
        entidad.setId(id);
        return getRepositorio().save(entidad);
    }

    @Transactional
    public final boolean deleteById(long id) {
        // Se conserva el borrado lógico de la aplicación en vez de borrar físicamente la fila.
        E entidad = findById(id);
        entidad.desactivar();
        getRepositorio().save(entidad);
        return true;
    }
}
