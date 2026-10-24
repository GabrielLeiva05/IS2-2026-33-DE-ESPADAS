package com.uncuyo.tp1_ej4.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class controladorAuth {

    @GetMapping("/login")
    public String login(
            Model model,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout
    ) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contrasena incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesion cerrada correctamente");
        }
        return "views/login";
    }
}
