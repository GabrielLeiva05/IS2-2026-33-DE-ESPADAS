package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.service.PaisServicio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ejercicioIntegrador.tiendaderopa.service.ProvinciaServicio;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class AdminControlador {

    private final PaisServicio paisServicio;
    private final ProvinciaServicio provinciaServicio;
    public AdminControlador(PaisServicio paisServicio, ProvinciaServicio provinciaServicio) {
        this.paisServicio = paisServicio;
        this.provinciaServicio = provinciaServicio;
    }
    @GetMapping("/dashboard")
    public String dashboard(ModelMap modelo) {
        modelo.addAttribute("paises", paisServicio.listarTodos());
        modelo.addAttribute("pais", null);
        modelo.addAttribute("provincias", provinciaServicio.listarTodas());
        modelo.addAttribute("provincia", null);
        return "panel.html";
    }
}