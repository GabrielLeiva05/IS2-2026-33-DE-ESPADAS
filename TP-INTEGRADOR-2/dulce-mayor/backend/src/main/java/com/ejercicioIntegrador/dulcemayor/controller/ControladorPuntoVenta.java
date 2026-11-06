package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPuntoVenta;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/preventista/puntos-venta")
public class ControladorPuntoVenta {

    private final ServicioPuntoVenta servicioPuntoVenta;

    public ControladorPuntoVenta(ServicioPuntoVenta servicioPuntoVenta) {
        this.servicioPuntoVenta = servicioPuntoVenta;
    }

    @GetMapping
    public ResponseEntity<List<PuntoVentaResponseDTO>> listar(Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPuntoVenta.listarCartera(authentication.getName()));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<PuntoVentaResponseDTO>> listarCliente(@PathVariable String idCliente,
                                                                       Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPuntoVenta.listarPuntosCliente(idCliente, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<PuntoVentaResponseDTO> registrar(@RequestBody PuntoVentaRequestDTO dto,
                                                             Authentication authentication) throws MiException {
        PuntoVentaResponseDTO creado = servicioPuntoVenta.registrar(dto, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }
}
