package com.incisog.Clinica.controller;


import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.PacienteService;
import com.incisog.Clinica.models.Paciente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {
    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Paciente>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paciente> buscar(@PathVariable String id) throws ErrorServiceException {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Paciente> crear(@RequestBody Paciente paciente) throws ErrorServiceException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(paciente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> modificar(@PathVariable String id, @RequestBody Paciente paciente) throws ErrorServiceException {
        return ResponseEntity.ok(service.modificar(id, paciente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) throws ErrorServiceException {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
