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
@RequestMapping("/persona")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class PersonaAbmControlador {

    private final PersonaServicio personaServicio;

    public PersonaAbmControlador(PersonaServicio personaServicio) {
        this.personaServicio = personaServicio;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String apellido,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
                           @RequestParam TipoDocumento tipoDocumento,
                           @RequestParam String documento,
                           RedirectAttributes redirectAttributes) {
        try {
            personaServicio.crearPersona(nombre, apellido, fechaNacimiento, tipoDocumento, documento);
            redirectAttributes.addFlashAttribute("exito", "Persona cargada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("persona", personaServicio.buscarPersona(id));
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
                           @RequestParam String nombre,
                           @RequestParam String apellido,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
                           @RequestParam TipoDocumento tipoDocumento,
                           @RequestParam String documento,
                           RedirectAttributes redirectAttributes) {
        try {
            personaServicio.modificarPersona(id, nombre, apellido, fechaNacimiento, tipoDocumento, documento);
            redirectAttributes.addFlashAttribute("exito", "Persona modificada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            personaServicio.eliminarPersona(id);
            redirectAttributes.addFlashAttribute("exito", "Persona eliminada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
