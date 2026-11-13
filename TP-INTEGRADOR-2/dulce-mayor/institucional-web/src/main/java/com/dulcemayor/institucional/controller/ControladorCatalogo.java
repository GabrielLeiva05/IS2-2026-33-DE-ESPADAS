package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.service.ServicioCatalogo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorCatalogo {

    private final ServicioCatalogo servicioCatalogo;

    public ControladorCatalogo(ServicioCatalogo servicioCatalogo) {
        this.servicioCatalogo = servicioCatalogo;
    }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String categoria,
                           @RequestParam(required = false) String texto,
                           @RequestParam(defaultValue = "false") boolean ofertas,
                           Model model) {
        model.addAttribute("productos", servicioCatalogo.buscar(categoria, texto, ofertas));
        model.addAttribute("categorias", servicioCatalogo.categorias());
        model.addAttribute("cotizacion", servicioCatalogo.cotizacion().orElse(null));
        model.addAttribute("categoria", categoria);
        model.addAttribute("texto", texto);
        model.addAttribute("ofertas", ofertas);
        return "catalogo";
    }
}
