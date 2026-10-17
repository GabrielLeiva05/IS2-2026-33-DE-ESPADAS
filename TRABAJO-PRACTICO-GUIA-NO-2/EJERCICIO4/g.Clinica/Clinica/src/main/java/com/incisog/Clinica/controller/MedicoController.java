package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.dto.MedicoDTO;
import com.incisog.Clinica.logic.dto.adapter.MedicoAdapter;
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
    private final MedicoAdapter adapter;

    public MedicoController(MedicoService service, MedicoAdapter adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    @GetMapping
    public ResponseEntity<List<MedicoDTO>> listarMedicos() {
        return ResponseEntity.ok(adapter.toDTOList(service.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoDTO> buscarMedico(@PathVariable String id) throws ErrorServiceException {
        return ResponseEntity.ok(adapter.toDTO(service.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<MedicoDTO> darAlta(@RequestBody MedicoDTO dto) throws ErrorServiceException {
        Medico creado = service.crear(adapter.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(adapter.toDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoDTO> modificar(@PathVariable String id, @RequestBody MedicoDTO dto) throws ErrorServiceException {
        Medico modificado = service.modificar(id, adapter.toEntity(dto));
        return ResponseEntity.ok(adapter.toDTO(modificado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darBaja(@PathVariable String id) throws ErrorServiceException {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
