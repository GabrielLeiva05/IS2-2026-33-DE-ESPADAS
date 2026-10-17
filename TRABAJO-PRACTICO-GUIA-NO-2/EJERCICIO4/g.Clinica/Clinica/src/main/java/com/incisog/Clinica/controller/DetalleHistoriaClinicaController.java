package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.DetalleHistoriaClinicaService;
import com.incisog.Clinica.models.DetalleHistoriaClinica;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historias/{historiaId}/detalles")
public class DetalleHistoriaClinicaController {
    private final DetalleHistoriaClinicaService service;

    public DetalleHistoriaClinicaController(DetalleHistoriaClinicaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DetalleHistoriaClinica>> listar(
            @PathVariable String historiaId,
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false) String valor) throws ErrorServiceException {

        if (criterio == null || criterio.isBlank()) {
            return ResponseEntity.ok(service.listar(historiaId));              // sin filtro: como siempre
        }
        return ResponseEntity.ok(service.buscar(historiaId, criterio, valor)); // con filtro: Strategy
    }

    // Body: {"fechaHistoria": "2026-10-13", "detalleHistoria": "...", "medico": {"id": "..."}}
    @PostMapping
    public ResponseEntity<DetalleHistoriaClinica> agregar(@PathVariable String historiaId,
                                                          @RequestBody DetalleHistoriaClinica detalle) throws ErrorServiceException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agregar(historiaId, detalle));
    }

    @PutMapping("/{detalleId}")
    public ResponseEntity<DetalleHistoriaClinica> modificar(@PathVariable String historiaId,
                                                            @PathVariable String detalleId,
                                                            @RequestBody DetalleHistoriaClinica detalle) throws ErrorServiceException {
        return ResponseEntity.ok(service.modificar(historiaId, detalleId, detalle));
    }

    @DeleteMapping("/{detalleId}")
    public ResponseEntity<Void> eliminar(@PathVariable String historiaId,
                                         @PathVariable String detalleId) throws ErrorServiceException {
        service.eliminar(historiaId, detalleId);
        return ResponseEntity.noContent().build();
    }
}
