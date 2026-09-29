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
@RequestMapping("/contacto-telefonico")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class ContactoTelefonicoAbmControlador {

    private final ServicioContactoTelefonico servicioContactoTelefonico;
    private final ServicioContacto servicioContacto;
    private final PersonaServicio personaServicio;

    public ContactoTelefonicoAbmControlador(ServicioContactoTelefonico servicioContactoTelefonico, ServicioContacto servicioContacto, PersonaServicio personaServicio) {
        this.servicioContactoTelefonico = servicioContactoTelefonico;
        this.servicioContacto = servicioContacto;
        this.personaServicio = personaServicio;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String idPersona,
                           @RequestParam String telefono,
                           @RequestParam TipoTelefono tipoTelefono,
                           @RequestParam TipoContacto tipoContacto,
                           @RequestParam(required = false) String observacion,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioContactoTelefonico.crearContactoTelefonico(telefono, tipoTelefono, tipoContacto, observacion, idPersona);
            redirectAttributes.addFlashAttribute("exito", "Contacto telefónico cargado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("contactoTelefonico", servicioContactoTelefonico.buscarContactoTelefonico(id));
            modelo.addAttribute("contactosTelefonicos", servicioContactoTelefonico.listarContactoTelefonico());
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
                           @RequestParam String telefono,
                           @RequestParam TipoTelefono tipoTelefono,
                           @RequestParam TipoContacto tipoContacto,
                           @RequestParam(required = false) String observacion,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioContactoTelefonico.modificarContactoTelefonico(id, telefono, tipoTelefono, tipoContacto, observacion);
            redirectAttributes.addFlashAttribute("exito", "Contacto telefónico modificado correctamente.");
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
            redirectAttributes.addFlashAttribute("exito", "Contacto telefónico eliminado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
