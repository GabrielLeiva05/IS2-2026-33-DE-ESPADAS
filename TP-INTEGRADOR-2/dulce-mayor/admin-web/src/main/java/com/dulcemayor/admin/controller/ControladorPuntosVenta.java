package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioPuntosVenta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Listado de todos los puntos de venta con su ubicación y responsable. */
@Controller
@RequestMapping("/puntos-venta")
public class ControladorPuntosVenta {

    private final ServicioPuntosVenta servicioPuntosVenta;
    private final GestorSesion gestorSesion;

    public ControladorPuntosVenta(ServicioPuntosVenta servicioPuntosVenta, GestorSesion gestorSesion) {
        this.servicioPuntosVenta = servicioPuntosVenta;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(HttpServletRequest request, Model model) {
        model.addAttribute("puntos", servicioPuntosVenta.listar(
                gestorSesion.actual(request).orElseThrow().getToken()));
        return "puntos-venta/lista";
    }
}
