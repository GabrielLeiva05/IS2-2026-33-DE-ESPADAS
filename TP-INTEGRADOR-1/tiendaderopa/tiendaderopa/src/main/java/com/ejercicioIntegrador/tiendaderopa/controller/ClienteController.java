package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Cliente;
import com.ejercicioIntegrador.tiendaderopa.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @PostMapping
    public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
        return ResponseEntity.ok(service.crearCliente(
                cliente.getNombre(), 
                cliente.getApellido(), 
                cliente.getFechaNacimiento(),
                cliente.getTipoDocumento(), 
                cliente.getDocumento(), 
                cliente.getDireccionEstadia()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> modificar(@PathVariable String id, @RequestBody Cliente cliente) {
        return ResponseEntity.ok(service.modificarCliente(
                id, 
                cliente.getNombre(), 
                cliente.getApellido(), 
                cliente.getFechaNacimiento(),
                cliente.getTipoDocumento(), 
                cliente.getDocumento(), 
                cliente.getDireccionEstadia()
        ));
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(service.listarCliente());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Cliente>> listarActivos() {
        return ResponseEntity.ok(service.listarClienteActivo());
    }
}