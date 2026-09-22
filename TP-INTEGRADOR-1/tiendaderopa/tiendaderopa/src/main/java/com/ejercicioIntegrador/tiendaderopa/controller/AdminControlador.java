package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.service.PaisServicio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class AdminControlador {

    private final PaisServicio paisServicio;

    public AdminControlador(PaisServicio paisServicio) {
        this.paisServicio = paisServicio;
    }

    @GetMapping("/dashboard")
    public String dashboard(ModelMap modelo) {
        modelo.addAttribute("paises", paisServicio.listarTodos());
        modelo.addAttribute("pais", null);
        return "panel.html";
    }
}