package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.SuscripcionNewsletterResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SuscripcionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioNewsletter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Alta y baja del newsletter mensual. Ambos endpoints son públicos (opt-in con baja por token). */
@RestController
@RequestMapping("/api/newsletter")
public class ControladorNewsletter {

    private final ServicioNewsletter servicioNewsletter;

    public ControladorNewsletter(ServicioNewsletter servicioNewsletter) {
        this.servicioNewsletter = servicioNewsletter;
    }

    @PostMapping
    public ResponseEntity<SuscripcionNewsletterResponseDTO> suscribir(@RequestBody SuscripcionRequestDTO dto)
            throws MiException {
        return ResponseEntity.ok(servicioNewsletter.suscribir(dto == null ? null : dto.email()));
    }

    @GetMapping("/baja")
    public ResponseEntity<SuscripcionNewsletterResponseDTO> baja(@RequestParam String token) throws MiException {
        return ResponseEntity.ok(servicioNewsletter.darDeBaja(token));
    }
}
