package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.EntidadBase;
import com.uncuyo.tp1_ej4.services.ServicioBase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Template Method de la API REST: fija el flujo HTTP común de CRUD y deja a
 * cada controlador concretar el servicio y, si corresponde, el listado.
 */
public abstract class RestControllerBase<E extends EntidadBase> {

    private final ServicioBase<E> servicio;

    protected RestControllerBase(ServicioBase<E> servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public final List<E> listar(@RequestParam(required = false) String query) {
        return listarRecursos(query);
    }

    protected List<E> listarRecursos(String query) {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    public final E buscarPorId(@PathVariable long id) {
        return servicio.findById(id);
    }

    @PostMapping
    public final ResponseEntity<E> crear(@Valid @RequestBody E entidad) {
        E creada = servicio.saveOne(entidad);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    public final E actualizar(@PathVariable long id, @Valid @RequestBody E entidad) {
        return servicio.updateOne(entidad, id);
    }

    @DeleteMapping("/{id}")
    public final ResponseEntity<Void> eliminar(@PathVariable long id) {
        servicio.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
