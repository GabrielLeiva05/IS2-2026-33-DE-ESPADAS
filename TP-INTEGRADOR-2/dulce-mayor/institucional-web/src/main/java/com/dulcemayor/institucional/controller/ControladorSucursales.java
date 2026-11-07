package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.service.ServicioSitio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Página pública de sucursales con su ubicación. */
@Controller
public class ControladorSucursales {

    private final ServicioSitio servicioSitio;

    public ControladorSucursales(ServicioSitio servicioSitio) {
        this.servicioSitio = servicioSitio;
    }

    @GetMapping("/sucursales")
    public String sucursales(Model model) {
        model.addAttribute("sucursales", servicioSitio.sucursales());
        model.addAttribute("cotizacion", servicioSitio.cotizacion());
        return "sucursales";
    }
}
