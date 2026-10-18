package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.EntidadBase;
import ar.com.biblioteca.server.exceptions.ResourceNotFoundException;
import ar.com.biblioteca.server.repositories.RepositorioBase;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class ServicioBase<E extends EntidadBase> {

    protected final RepositorioBase<E> repository;

    protected ServicioBase(RepositorioBase<E> repository) {
        this.repository = repository;
    }

    // ===================== MÉTODOS PLANTILLA =====================

    @Transactional(readOnly = true)
    public List<E> findAll(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return repository.findAll(ordenamiento());
        }
        return buscarPorFiltro(filtro.trim());
    }

    @Transactional(readOnly = true)
    public E findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe " + nombreEntidad() + " con id " + id));
    }

    @Transactional
    public E create(E entidad) {
        entidad.setId(null);
        antesDeCrear(entidad);                 // hook
        return repository.save(entidad);
    }

    @Transactional
    public E update(Long id, E datos) {
        E existente = findById(id);
        antesDeActualizar(id, datos);          // hook
        copiarDatos(existente, datos);         // paso obligatorio
        return repository.saveAndFlush(existente);
    }

    @Transactional
    public void delete(Long id) {
        E existente = findById(id);
        antesDeEliminar(existente);            // hook
        repository.delete(existente);
    }

    /** Obligatorios: cada servicio concreto los define. */
    protected abstract String nombreEntidad();             // ej: "la localidad"
    protected abstract void copiarDatos(E destino, E origen);

    /** Opcionales (hooks): tienen comportamiento por defecto. */
    protected Sort ordenamiento() { return Sort.by("id"); }
    protected List<E> buscarPorFiltro(String filtro) { return repository.findAll(ordenamiento()); }
    protected void antesDeCrear(E entidad) { }
    protected void antesDeActualizar(Long id, E datos) { }
    protected void antesDeEliminar(E existente) { }
}