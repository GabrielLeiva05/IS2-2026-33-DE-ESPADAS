package com.competencia.blockazo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sirve el frontend React compilado (src/main/resources/static) desde el mismo backend.
 * Las rutas del lado del cliente (React Router) se reenvian a index.html para que
 * al recargar /perfil o entrar directo a /login no de 404.
 */
@Controller
public class SpaController {

    @GetMapping({"/", "/login", "/registro", "/perfil", "/admin/usuarios", "/auth/callback"})
    public String index() {
        return "forward:/index.html";
    }
}
