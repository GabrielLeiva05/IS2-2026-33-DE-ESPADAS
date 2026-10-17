package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.dto.HistoriaClinicaDTO;
import com.incisog.Clinica.logic.dto.adapter.HistoriaClinicaAdapter;
import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.HistoriaClinicaService;
import com.incisog.Clinica.models.HistoriaClinica;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * No hay PUT: la historia no tiene datos propios editables; se editan sus detalles.
 */
@RestController
@RequestMapping("/api/historias")
public class HistoriaClinicaController {

    private final HistoriaClinicaService service;
    private final HistoriaClinicaAdapter adapter;

    public HistoriaClinicaController(HistoriaClinicaService service, HistoriaClinicaAdapter adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    @GetMapping
    public ResponseEntity<List<HistoriaClinicaDTO>> listar() {
        return ResponseEntity.ok(adapter.toDTOList(service.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoriaClinicaDTO> buscar(@PathVariable String id) throws ErrorServiceException {
        return ResponseEntity.ok(adapter.toDTO(service.buscarPorId(id)));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<HistoriaClinicaDTO> buscarPorPaciente(@PathVariable String pacienteId) throws ErrorServiceException {
        return ResponseEntity.ok(adapter.toDTO(service.buscarPorPaciente(pacienteId)));
    }

    // Body: {"pacienteId": "...", "usuarioId": "..."}
    @PostMapping
    public ResponseEntity<HistoriaClinicaDTO> crear(@RequestBody HistoriaClinicaDTO dto) throws ErrorServiceException {
        HistoriaClinica creada = service.crear(adapter.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(adapter.toDTO(creada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) throws ErrorServiceException {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
