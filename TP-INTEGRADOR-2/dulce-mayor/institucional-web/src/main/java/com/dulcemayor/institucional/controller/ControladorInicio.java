package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.service.ServicioCatalogo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorInicio {

    private final ServicioCatalogo servicioCatalogo;

    public ControladorInicio(ServicioCatalogo servicioCatalogo) {
        this.servicioCatalogo = servicioCatalogo;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("categorias", servicioCatalogo.categorias());
        model.addAttribute("cotizacion", servicioCatalogo.cotizacion().orElse(null));
        model.addAttribute("ofertas", servicioCatalogo.ofertasDestacadas());
        model.addAttribute("destacados", servicioCatalogo.destacados());
        return "inicio";
    }
}
