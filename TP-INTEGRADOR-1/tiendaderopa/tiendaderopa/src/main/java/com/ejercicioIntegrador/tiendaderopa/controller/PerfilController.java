package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.dto.ActualizarPerfilDTO;
import com.ejercicioIntegrador.tiendaderopa.dto.PerfilDTO;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.service.PerfilServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/perfil")
public class PerfilController {

    private final PerfilServicio perfilServicio;

    public PerfilController(PerfilServicio perfilServicio) {
        this.perfilServicio = perfilServicio;
    }

    @GetMapping
    public PerfilDTO obtener(@AuthenticationPrincipal UserDetails usuario) throws MiException {
        return perfilServicio.obtener(usuario.getUsername());
    }

    @PutMapping
    public PerfilDTO actualizar(@AuthenticationPrincipal UserDetails usuario,
            @Valid @RequestBody ActualizarPerfilDTO datos) throws MiException {
        return perfilServicio.actualizar(usuario.getUsername(), datos);
    }
}