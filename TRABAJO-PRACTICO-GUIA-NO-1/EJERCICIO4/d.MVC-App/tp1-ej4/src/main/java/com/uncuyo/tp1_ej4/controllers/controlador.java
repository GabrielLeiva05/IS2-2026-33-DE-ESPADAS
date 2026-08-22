package com.uncuyo.tp1_ej4.controllers;

import com.uncuyo.tp1_ej4.services.ServicioCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class controlador {

    @Autowired
    private ServicioCategoria svcCategoria;

    @GetMapping(value = "/")
    public String index(Model model) {
        try {
            model.addAttribute("categorias", this.svcCategoria.findAll());
            return "index";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
