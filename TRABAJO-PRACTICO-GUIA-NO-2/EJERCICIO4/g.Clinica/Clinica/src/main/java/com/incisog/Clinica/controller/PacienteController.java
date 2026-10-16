package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.dto.PacienteDTO;
import com.incisog.Clinica.logic.dto.adapter.PacienteAdapter;
import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.PacienteService;
import com.incisog.Clinica.models.Paciente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * El controller es el BORDE de la API:
 *   - hacia afuera (JSON) habla en PacienteDTO,
 *   - hacia adentro (service) habla en Paciente (entidad).
 * El adapter traduce en el medio. El service y su Template Method no cambian.
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService service;
    private final PacienteAdapter adapter;

    public PacienteController(PacienteService service, PacienteAdapter adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    @GetMapping
    public ResponseEntity<List<PacienteDTO>> listar() {
        return ResponseEntity.ok(adapter.toDTOList(service.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> buscar(@PathVariable String id) throws ErrorServiceException {
        return ResponseEntity.ok(adapter.toDTO(service.buscarPorId(id)));
    }

    //Recordar, React envia texto JSON por HTTP, pero en el Backend es Jackson quien arma el objeto PacienteDTO
    //Segun la coincidencia de los nombres de keys con atributos
    @PostMapping
    public ResponseEntity<PacienteDTO> crear(@RequestBody PacienteDTO dto) throws ErrorServiceException {
        Paciente creado = service.crear(adapter.toEntity(dto));      // DTO → entidad → service
        return ResponseEntity.status(HttpStatus.CREATED).body(adapter.toDTO(creado)); // entidad → DTO
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteDTO> modificar(@PathVariable String id, @RequestBody PacienteDTO dto) throws ErrorServiceException {
        Paciente modificado = service.modificar(id, adapter.toEntity(dto));
        return ResponseEntity.ok(adapter.toDTO(modificado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) throws ErrorServiceException {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
