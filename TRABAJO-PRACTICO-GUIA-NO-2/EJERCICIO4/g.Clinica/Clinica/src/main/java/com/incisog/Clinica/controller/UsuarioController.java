package com.incisog.Clinica.controller;

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

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscar(@PathVariable String id) throws ErrorServiceException {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) throws ErrorServiceException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> modificar(@PathVariable String id, @RequestBody Usuario usuario) throws ErrorServiceException {
        return ResponseEntity.ok(service.modificar(id, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) throws ErrorServiceException {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Login: POST porque la clave viaja en el body (en un GET quedaría en la URL).
    // Body: {"nombreUsuario": "...", "clave": "..."}  → 200 con el usuario (sin clave) o 400.
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario credenciales) throws ErrorServiceException {
        return ResponseEntity.ok(service.login(credenciales.getNombreUsuario(), credenciales.getClave()));
    }

}
