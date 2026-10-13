package ar.com.biblioteca.server.controllers;

import ar.com.biblioteca.server.entities.EntidadBase;
import ar.com.biblioteca.server.services.ServicioBase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class ControladorBase<E extends EntidadBase, S extends ServicioBase<E>> {

    protected final S service;

    protected ControladorBase(S service) {
        this.service = service;
    }

    @GetMapping
    public List<E> listar(@RequestParam(name = "filtro", required = false) String filtro) {
        return service.findAll(filtro);
    }

    @GetMapping("/{id}")
    public E obtener(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<E> crear(@Valid @RequestBody E entidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(entidad));
    }

    @PutMapping("/{id}")
    public E actualizar(@PathVariable("id") Long id, @Valid @RequestBody E entidad) {
        return service.update(id, entidad);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") Long id) {
        service.delete(id);
    }
}