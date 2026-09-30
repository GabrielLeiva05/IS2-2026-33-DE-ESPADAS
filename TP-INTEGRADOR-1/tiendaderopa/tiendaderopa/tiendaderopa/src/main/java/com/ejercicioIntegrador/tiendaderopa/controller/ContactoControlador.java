package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Contacto;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioContacto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/contactos")
public class ContactoControlador {

    private final ServicioContacto contactoServicio;

    public ContactoControlador(ServicioContacto contactoServicio) {
        this.contactoServicio = contactoServicio;
    }

    @GetMapping("/{id}")
    public String buscarContacto(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            Contacto contacto = contactoServicio.buscarContacto(id);
            AdminPageSupport.cargar(model, "Contacto", "/admin/contactos", Contacto.class,
                    java.util.List.of(contacto), java.util.List.of(), null);
            model.addAttribute("permitirCrear", false);
            model.addAttribute("permitirEditar", false);
            return "admin/registros";
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/contactos/correo";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarContacto(@PathVariable String id, RedirectAttributes redirect) {
        try {
            contactoServicio.eliminarContacto(id);
            redirect.addFlashAttribute("mensaje", "Contacto eliminado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contactos/correo";
    }
}
