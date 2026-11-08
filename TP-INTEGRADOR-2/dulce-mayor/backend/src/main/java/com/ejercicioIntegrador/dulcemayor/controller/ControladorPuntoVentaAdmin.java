package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPuntoVenta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Puntos de venta de todos los preventistas (consulta administrativa). */
@RestController
@RequestMapping("/api/puntos-venta")
public class ControladorPuntoVentaAdmin {

    private final ServicioPuntoVenta servicioPuntoVenta;

    public ControladorPuntoVentaAdmin(ServicioPuntoVenta servicioPuntoVenta) {
        this.servicioPuntoVenta = servicioPuntoVenta;
    }

    @GetMapping
    public ResponseEntity<List<PuntoVentaResponseDTO>> listar() {
        return ResponseEntity.ok(servicioPuntoVenta.listarTodos());
    }
}
