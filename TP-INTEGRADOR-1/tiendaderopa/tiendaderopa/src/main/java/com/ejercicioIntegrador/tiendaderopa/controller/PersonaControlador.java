package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.service.PersonaServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/personas")
public class PersonaControlador {

    private final PersonaServicio servicio;

    public PersonaControlador(PersonaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Persona> listar() {
        return servicio.listarPersona();
    }

    @GetMapping("/activas")
    public List<Persona> listarActivas() {
        return servicio.listarPersona();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String id) {
        try {
            Persona persona = servicio.buscarPersona(id);
            return ResponseEntity.ok(persona);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String documento
    ) {
        try {
            Persona persona = servicio.crearPersona(nombre, apellido, fechaNacimiento, tipoDocumento, documento);
            return ResponseEntity.status(HttpStatus.CREATED).body(persona);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String documento
    ) {
        try {
            Persona persona = servicio.modificarPersona(id, nombre, apellido, fechaNacimiento, tipoDocumento, documento);
            return ResponseEntity.ok(persona);
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            servicio.eliminarPersona(id);
            return ResponseEntity.noContent().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{idPersona}/usuario/{idUsuario}")
    public ResponseEntity<?> asignarUsuario(@PathVariable String idPersona, @PathVariable String idUsuario) {
        try {
            servicio.asignarUsuario(idPersona, idUsuario);
            return ResponseEntity.ok().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{idPersona}/usuario")
    public ResponseEntity<?> removerUsuario(@PathVariable String idPersona) {
        try {
            servicio.removerUsuario(idPersona);
            return ResponseEntity.noContent().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}