package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.MedicoService;
import com.incisog.Clinica.models.Medico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service){
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Medico>> listarMedicos(){
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medico> buscarMedico(@PathVariable String id) throws ErrorServiceException {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Medico> darAlta(@RequestBody Medico medico) throws ErrorServiceException {
        service.crear(medico);
        return ResponseEntity.status(HttpStatus.CREATED).body(medico);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Medico> modificar(@PathVariable String id, @RequestBody Medico medico) throws ErrorServiceException {
        return ResponseEntity.ok(service.modificar(id, medico));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Medico> darBaja(@PathVariable String id) throws ErrorServiceException {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
