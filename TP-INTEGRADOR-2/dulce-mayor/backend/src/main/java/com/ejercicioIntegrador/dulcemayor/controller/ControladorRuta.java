package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.AsignarRutaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioRuta;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Asignación de pedidos a hojas de ruta (personal administrativo; el jefe lo hereda). */
@RestController
@RequestMapping("/api/rutas")
public class ControladorRuta {

    private final ServicioRuta servicioRuta;

    public ControladorRuta(ServicioRuta servicioRuta) {
        this.servicioRuta = servicioRuta;
    }

    @GetMapping
    public ResponseEntity<List<RutaResponseDTO>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(servicioRuta.listar(fecha));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaResponseDTO> detalle(@PathVariable String id) throws MiException {
        return ResponseEntity.ok(servicioRuta.detalle(id));
    }

    @PostMapping
    public ResponseEntity<RutaResponseDTO> asignar(@RequestBody AsignarRutaRequestDTO dto) throws MiException {
        return ResponseEntity.ok(servicioRuta.asignar(dto));
    }
}
