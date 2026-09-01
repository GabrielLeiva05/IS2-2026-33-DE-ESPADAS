package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.services.ServicioVideojuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class controlador {

    @Autowired
    private ServicioVideojuego svcVideojuego;

    @GetMapping(value = "/")
    public String index(Model model) {
        try {
            model.addAttribute("videojuegos", this.svcVideojuego.findAllByActivo());
            return "index";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
