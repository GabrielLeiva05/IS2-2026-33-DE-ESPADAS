package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.*;
import com.ejercicioIntegrador.tiendaderopa.model.*;
import com.ejercicioIntegrador.tiendaderopa.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Date;

@Controller
@RequestMapping("/contacto-correo")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class ContactoCorreoElectronicoAbmControlador {

    private final ServicioContactoCorreoElectronico servicioContactoCorreo;
    private final ServicioContacto servicioContacto;
    private final PersonaServicio personaServicio;

    public ContactoCorreoElectronicoAbmControlador(ServicioContactoCorreoElectronico servicioContactoCorreo, ServicioContacto servicioContacto, PersonaServicio personaServicio) {
        this.servicioContactoCorreo = servicioContactoCorreo;
        this.servicioContacto = servicioContacto;
        this.personaServicio = personaServicio;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String idPersona,
                           @RequestParam String email,
                           @RequestParam TipoContacto tipoContacto,
                           @RequestParam(required = false) String observacion,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioContactoCorreo.crearContactoCorreoElectronico(email, tipoContacto, observacion, idPersona);
            redirectAttributes.addFlashAttribute("exito", "Contacto de correo cargado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("contactoCorreo", servicioContactoCorreo.buscarContactoCorreoElectronico(id));
            modelo.addAttribute("contactosCorreo", servicioContactoCorreo.listarContactoCorreoElectronico());
            modelo.addAttribute("personas", personaServicio.listarTodas());
            return "panel.html";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                           @RequestParam String email,
                           @RequestParam TipoContacto tipoContacto,
                           @RequestParam(required = false) String observacion,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioContactoCorreo.modificarContactoCorreoElectronico(id, email, tipoContacto, observacion);
            redirectAttributes.addFlashAttribute("exito", "Contacto de correo modificado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioContacto.eliminarContacto(id);
            redirectAttributes.addFlashAttribute("exito", "Contacto de correo eliminado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
