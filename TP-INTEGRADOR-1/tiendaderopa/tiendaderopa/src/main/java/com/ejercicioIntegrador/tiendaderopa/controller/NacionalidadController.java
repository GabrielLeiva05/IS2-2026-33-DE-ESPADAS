package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Nacionalidad;
import com.ejercicioIntegrador.tiendaderopa.service.NacionalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nacionalidades")
@RequiredArgsConstructor
public class NacionalidadController {

    private final NacionalidadService service;

    @PostMapping
    public ResponseEntity<Nacionalidad> crear(@RequestBody Nacionalidad nacionalidad) {
        return ResponseEntity.ok(service.crearNacionalidad(nacionalidad.getNombre()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nacionalidad> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarNacionalidad(id));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Nacionalidad> buscarPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(service.buscarNacionalidadPorNombre(nombre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Nacionalidad> modificar(@PathVariable String id, @RequestBody Nacionalidad nacionalidad) {
        return ResponseEntity.ok(service.modificarNacionalidad(id, nacionalidad.getNombre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminarNacionalidad(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Nacionalidad>> listarTodas() {
        return ResponseEntity.ok(service.listarNacionalidad());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Nacionalidad>> listarActivas() {
        return ResponseEntity.ok(service.listarNacionalidadActiva());
    }
}