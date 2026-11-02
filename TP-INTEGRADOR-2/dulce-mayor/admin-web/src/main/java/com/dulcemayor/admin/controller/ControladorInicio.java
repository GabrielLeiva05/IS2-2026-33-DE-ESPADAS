package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioPanel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorInicio {

    private final ServicioPanel servicioPanel;
    private final GestorSesion gestorSesion;

    public ControladorInicio(ServicioPanel servicioPanel, GestorSesion gestorSesion) {
        this.servicioPanel = servicioPanel;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping("/")
    public String inicio(HttpServletRequest request, Model model) {
        model.addAttribute("panel", servicioPanel.armar(gestorSesion.actual(request).orElseThrow()));
        return "inicio";
    }
}
