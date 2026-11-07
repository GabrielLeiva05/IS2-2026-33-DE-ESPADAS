package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.ReordenarVisitasRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VisitaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVisita;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Planificación de visitas del preventista autenticado. */
@RestController
@RequestMapping("/api/preventista/visitas")
public class ControladorVisitas {

    private final ServicioVisita servicioVisita;

    public ControladorVisitas(ServicioVisita servicioVisita) {
        this.servicioVisita = servicioVisita;
    }

    @GetMapping
    public ResponseEntity<VisitaResponseDTO.PlanVisitasDTO> plan(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioVisita.planDelDia(authentication.getName(), fecha));
    }

    @PostMapping("/generar")
    public ResponseEntity<VisitaResponseDTO.PlanVisitasDTO> generar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioVisita.generar(authentication.getName(), fecha));
    }

    @PutMapping("/orden")
    public ResponseEntity<VisitaResponseDTO.PlanVisitasDTO> reordenar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestBody ReordenarVisitasRequestDTO dto, Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioVisita.reordenar(authentication.getName(), fecha, dto));
    }

    @DeleteMapping("/{idVisita}")
    public ResponseEntity<Void> quitar(@PathVariable String idVisita, Authentication authentication)
            throws MiException {
        servicioVisita.quitar(authentication.getName(), idVisita);
        return ResponseEntity.noContent().build();
    }
}
