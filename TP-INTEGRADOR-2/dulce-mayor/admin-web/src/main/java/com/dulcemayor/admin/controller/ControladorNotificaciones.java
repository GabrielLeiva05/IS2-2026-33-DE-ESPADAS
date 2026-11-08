package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioNotificaciones;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Correos enviados (o simulados) y suscripciones al newsletter. */
@Controller
@RequestMapping("/notificaciones")
public class ControladorNotificaciones {

    private final ServicioNotificaciones servicioNotificaciones;
    private final GestorSesion gestorSesion;

    public ControladorNotificaciones(ServicioNotificaciones servicioNotificaciones, GestorSesion gestorSesion) {
        this.servicioNotificaciones = servicioNotificaciones;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String ver(HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("correos", servicioNotificaciones.correos(token));
        model.addAttribute("suscripciones", servicioNotificaciones.suscripciones(token));
        return "notificaciones/index";
    }

    @PostMapping("/newsletter/enviar")
    public String enviarNewsletter(HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            int enviados = servicioNotificaciones.enviarNewsletter(token(request));
            redireccion.addFlashAttribute("exito", "Newsletter enviado a " + enviados + " suscriptores.");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/notificaciones";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
