package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.entities.Estudio;
import com.uncuyo.tp1_ej4.services.ServicioEstudio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** API REST de estudios; comparte el servicio y la persistencia con la interfaz web. */
@RestController
@RequestMapping("/api/estudios")
public class EstudioRestController {

    private final ServicioEstudio servicio;

    public EstudioRestController(ServicioEstudio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Estudio> listar() {
        return servicio.findAll();
    }

    @GetMapping("/{id}")
    public Estudio buscarPorId(@PathVariable long id) {
        return servicio.findById(id);
    }

    @PostMapping
    public ResponseEntity<Estudio> crear(@Valid @RequestBody Estudio estudio) {
        Estudio creado = servicio.saveOne(estudio);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public Estudio actualizar(@PathVariable long id, @Valid @RequestBody Estudio estudio) {
        return servicio.updateOne(estudio, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        servicio.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
