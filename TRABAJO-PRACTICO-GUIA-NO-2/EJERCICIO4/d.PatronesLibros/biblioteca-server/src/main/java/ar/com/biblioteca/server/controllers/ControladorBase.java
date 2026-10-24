package ar.com.biblioteca.server.controllers;

import ar.com.biblioteca.server.dto.adapter.BaseAdapter;
import ar.com.biblioteca.server.entities.EntidadBase;
import ar.com.biblioteca.server.services.ServicioBase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD REST común (Template Method del ejercicio anterior).
 * <p>
 * REFACTOR (Adapter): hacia afuera habla en DTO (D) y hacia adentro en entidad (E).
 * El adapter traduce en el borde; el service y sus métodos plantilla no cambian.
 *
 * @param <E> entidad
 * @param <D> DTO
 * @param <S> service
 */
public abstract class ControladorBase<E extends EntidadBase, D, S extends ServicioBase<E>> {

    protected final S service;
    protected final BaseAdapter<E, D> adapter;

    protected ControladorBase(S service, BaseAdapter<E, D> adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    @GetMapping
    public List<D> listar(@RequestParam(name = "filtro", required = false) String filtro) {
        return adapter.toDTOList(service.findAll(filtro));
    }

    @GetMapping("/{id}")
    public D obtener(@PathVariable("id") Long id) {
        return adapter.toDTO(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<D> crear(@Valid @RequestBody D dto) {
        E creada = service.create(adapter.toEntity(dto));          // DTO → entidad → service
        return ResponseEntity.status(HttpStatus.CREATED).body(adapter.toDTO(creada)); // entidad → DTO
    }

    @PutMapping("/{id}")
    public D actualizar(@PathVariable("id") Long id, @Valid @RequestBody D dto) {
        return adapter.toDTO(service.update(id, adapter.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
