package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.EnvioNewsletter;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioNewsletter;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/newsletter")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ControladorNewsletter {

    private final ServicioNewsletter servicio;

    public ControladorNewsletter(ServicioNewsletter servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/enviar")
    public String enviarAhora(RedirectAttributes redirect) {
        try {
            EnvioNewsletter envio = servicio.enviarNewsletter();
            redirect.addFlashAttribute("exitoNewsletter",
                    "Newsletter enviado a " + envio.getDestinatariosEnviados() + " clientes.");
        } catch (MiException e) {
            redirect.addFlashAttribute("errorNewsletter", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping(value = "/vista-previa", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String vistaPrevia() {
        return servicio.generarHtml();
    }
}