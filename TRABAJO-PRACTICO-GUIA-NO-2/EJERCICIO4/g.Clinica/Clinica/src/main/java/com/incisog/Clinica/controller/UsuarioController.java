package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.dto.UsuarioDTO;
import com.incisog.Clinica.logic.dto.adapter.UsuarioAdapter;
import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.UsuarioService;
import com.incisog.Clinica.models.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioAdapter adapter;

    public UsuarioController(UsuarioService service, UsuarioAdapter adapter) {
        this.service = service;
        this.adapter = adapter;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(adapter.toDTOList(service.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscar(@PathVariable String id) throws ErrorServiceException {
        return ResponseEntity.ok(adapter.toDTO(service.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@RequestBody UsuarioDTO dto) throws ErrorServiceException {
        Usuario creado = service.crear(adapter.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(adapter.toDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> modificar(@PathVariable String id, @RequestBody UsuarioDTO dto) throws ErrorServiceException {
        Usuario modificado = service.modificar(id, adapter.toEntity(dto));
        return ResponseEntity.ok(adapter.toDTO(modificado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) throws ErrorServiceException {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Body: {"nombreUsuario": "...", "clave": "..."} → 200 con el usuario (sin clave) o 400
    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@RequestBody UsuarioDTO credenciales) throws ErrorServiceException {
        Usuario usuario = service.login(credenciales.getNombreUsuario(), credenciales.getClave());
        return ResponseEntity.ok(adapter.toDTO(usuario));
    }
}
