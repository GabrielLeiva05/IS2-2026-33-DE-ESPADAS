package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.cliente.ClienteApi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Alta al newsletter mensual desde el pie del sitio. El alta se hace contra el endpoint público de
 * la API, que envía el correo de confirmación con el enlace de baja.
 */
@Controller
public class ControladorNewsletter {

    private final ClienteApi clienteApi;

    public ControladorNewsletter(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    @PostMapping("/newsletter")
    public String suscribir(@RequestParam String email, RedirectAttributes redireccion) {
        try {
            clienteApi.enviar(null, new SuscripcionSolicitud(email), Object.class, "/api/newsletter");
            redireccion.addFlashAttribute("exito",
                    "Listo: te suscribimos al newsletter. Vas a recibir un correo con el enlace de baja.");
        } catch (ApiException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    private record SuscripcionSolicitud(String email) {
    }
}
