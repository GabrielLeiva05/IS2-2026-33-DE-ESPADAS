package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioPedidosAdmin;
import com.dulcemayor.admin.service.ServicioRutasAdmin;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Hoja de ruta del día: asignación de pedidos a cada distribuidor. */
@Controller
@RequestMapping("/rutas")
public class ControladorRutas {

    private final ServicioRutasAdmin servicioRutas;
    private final ServicioPedidosAdmin servicioPedidos;
    private final GestorSesion gestorSesion;

    public ControladorRutas(ServicioRutasAdmin servicioRutas, ServicioPedidosAdmin servicioPedidos,
                            GestorSesion gestorSesion) {
        this.servicioRutas = servicioRutas;
        this.servicioPedidos = servicioPedidos;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String fecha, HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("rutas", servicioRutas.listar(token, fecha));
        model.addAttribute("distribuidores", servicioRutas.distribuidores(token));
        model.addAttribute("pedidosAsignables", servicioRutas.pedidosAsignables(token));
        model.addAttribute("fecha", fecha);
        return "rutas/lista";
    }

    @PostMapping
    public String asignar(@RequestParam(required = false) String fecha,
                          @RequestParam String idDistribuidor,
                          @RequestParam(required = false) List<String> idsPedidos,
                          @RequestParam(required = false) String observacion,
                          HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            var ruta = servicioRutas.asignar(token(request), fecha, idDistribuidor, idsPedidos, observacion);
            redireccion.addFlashAttribute("exito", "Se asignaron " + ruta.getParadas().size()
                    + " paradas a " + ruta.getNombreDistribuidor() + ".");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rutas";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
