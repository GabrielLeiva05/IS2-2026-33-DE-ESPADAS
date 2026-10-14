package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.HistoriaClinicaService;
import com.incisog.Clinica.models.HistoriaClinica;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historias")
public class HistoriaClinicaController {
    private final HistoriaClinicaService service;

    public HistoriaClinicaController(HistoriaClinicaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<HistoriaClinica>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoriaClinica> buscar(@PathVariable String id) throws ErrorServiceException {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // "¿Cuál es la historia de este paciente?" — lo va a usar el frontend al entrar a un paciente.
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<HistoriaClinica> buscarPorPaciente(@PathVariable String pacienteId) throws ErrorServiceException {
        return ResponseEntity.ok(service.buscarPorPaciente(pacienteId));
    }

    // Body: {"paciente": {"id": "..."}, "usuario": {"id": "..."}}
    @PostMapping
    public ResponseEntity<HistoriaClinica> crear(@RequestBody HistoriaClinica historia) throws ErrorServiceException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(historia));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) throws ErrorServiceException {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
