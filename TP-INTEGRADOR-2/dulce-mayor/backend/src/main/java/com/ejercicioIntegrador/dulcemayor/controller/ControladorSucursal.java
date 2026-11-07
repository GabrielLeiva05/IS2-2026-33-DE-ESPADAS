package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.SucursalRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SucursalResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSucursal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Sucursales: consulta pública de las activas y gestión administrativa del resto. */
@RestController
@RequestMapping("/api/sucursales")
public class ControladorSucursal {

    private final ServicioSucursal servicioSucursal;

    public ControladorSucursal(ServicioSucursal servicioSucursal) {
        this.servicioSucursal = servicioSucursal;
    }

    @GetMapping
    public ResponseEntity<List<SucursalResponseDTO>> listarActivas() {
        return ResponseEntity.ok(servicioSucursal.listarActivas());
    }

    @GetMapping("/todas")
    public ResponseEntity<List<SucursalResponseDTO>> listarTodas() {
        return ResponseEntity.ok(servicioSucursal.listarTodas());
    }

    @PostMapping
    public ResponseEntity<SucursalResponseDTO> alta(@RequestBody SucursalRequestDTO dto) throws MiException {
        SucursalResponseDTO creada = servicioSucursal.alta(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalResponseDTO> modificar(@PathVariable String id,
                                                         @RequestBody SucursalRequestDTO dto) throws MiException {
        return ResponseEntity.ok(servicioSucursal.modificar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable String id) throws MiException {
        servicioSucursal.baja(id);
        return ResponseEntity.noContent().build();
    }
}
