package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.Categoria;
import com.uncuyo.tp1_ej4.services.ServicioCategoria;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** API REST de categorías; comparte el servicio y la persistencia con la interfaz web. */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaRestController {

    private final ServicioCategoria servicio;

    public CategoriaRestController(ServicioCategoria servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Categoria> listar() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    public Categoria buscarPorId(@PathVariable long id) {
        return servicio.findById(id);
    }

    @PostMapping
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria categoria) {
        Categoria creada = servicio.saveOne(categoria);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable long id, @Valid @RequestBody Categoria categoria) {
        return servicio.updateOne(categoria, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        servicio.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
