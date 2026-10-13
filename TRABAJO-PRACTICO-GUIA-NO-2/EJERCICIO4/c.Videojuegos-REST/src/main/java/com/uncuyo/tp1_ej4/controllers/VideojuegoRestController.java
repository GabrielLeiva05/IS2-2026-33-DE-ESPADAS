package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.Videojuego;
import com.uncuyo.tp1_ej4.services.ServicioVideojuego;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints JSON para videojuegos. Las páginas Thymeleaf existentes mantienen
 * sus rutas; los clientes REST consumen esta API bajo /api/videojuegos.
 */
@RestController
@RequestMapping("/api/videojuegos")
public class VideojuegoRestController {

    private final ServicioVideojuego servicio;

    public VideojuegoRestController(ServicioVideojuego servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Videojuego> listar(@RequestParam(required = false) String query) {
        // Sin filtro se consulta el inventario completo; con query se reutiliza la búsqueda de títulos activos.
        return query == null || query.isBlank()
                ? servicio.findAll()
                : servicio.findByTitle(query);
    }

    @GetMapping("/{id}")
    public Videojuego buscarPorId(@PathVariable long id) {
        return servicio.findById(id);
    }

    @PostMapping
    public ResponseEntity<Videojuego> crear(@Valid @RequestBody Videojuego videojuego) {
        Videojuego creado = servicio.saveOne(videojuego);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public Videojuego actualizar(
            @PathVariable long id,
            @Valid @RequestBody Videojuego videojuego
    ) {
        return servicio.updateOne(videojuego, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        // DELETE conserva el borrado lógico del dominio y responde sin cuerpo (204).
        servicio.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
