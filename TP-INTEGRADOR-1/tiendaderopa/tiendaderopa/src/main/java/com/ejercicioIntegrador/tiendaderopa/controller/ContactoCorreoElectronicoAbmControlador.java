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

/**
 * ABM de contactos de correo embebido en el dashboard (fragmento fragments/abmContactoCorreo.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/contactos/correo) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/contacto-correo")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ContactoCorreoElectronicoAbmControlador {

    private final ServicioContactoCorreoElectronico servicioContactoCorreo;
    private final ServicioContacto servicioContacto;
    private final PersonaServicio personaServicio;
    private final ServicioProveedor servicioProveedor;

    public ContactoCorreoElectronicoAbmControlador(ServicioContactoCorreoElectronico servicioContactoCorreo, ServicioContacto servicioContacto, PersonaServicio personaServicio, ServicioProveedor servicioProveedor) {
        this.servicioContactoCorreo = servicioContactoCorreo;
        this.servicioContacto = servicioContacto;
        this.personaServicio = personaServicio;
        this.servicioProveedor = servicioProveedor;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam(required = false) String idPersona,
                           @RequestParam(required = false) String idProveedor,
                           @RequestParam String email,
                           @RequestParam TipoContacto tipoContacto,
                           @RequestParam(required = false) String observacion,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioContactoCorreo.crearContactoCorreoElectronico(email, tipoContacto, observacion, vacioANull(idPersona), vacioANull(idProveedor));
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
            ContactoCorreoElectronico contacto = servicioContactoCorreo.listarContactoCorreoElectronico().stream()
                    .filter(actual -> actual.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new Exception("No existe un contacto de correo electrónico con id: " + id));
            modelo.addAttribute("contactoCorreo", contacto);
            modelo.addAttribute("contactosCorreo", servicioContactoCorreo.listarContactoCorreoElectronico());
            modelo.addAttribute("personas", personaServicio.listarPersona());
            modelo.addAttribute("proveedores", servicioProveedor.listarProveedorActivo());
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

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
