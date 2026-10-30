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
 * ABM de contactos telefónicos embebido en el dashboard (fragmento fragments/abmContactoTelefonico.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/contactos/telefono) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/contacto-telefonico")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ContactoTelefonicoAbmControlador {

    private final ServicioContactoTelefonico servicioContactoTelefonico;
    private final ServicioContacto servicioContacto;
    private final PersonaServicio personaServicio;
    private final ServicioProveedor servicioProveedor;

    public ContactoTelefonicoAbmControlador(ServicioContactoTelefonico servicioContactoTelefonico, ServicioContacto servicioContacto, PersonaServicio personaServicio, ServicioProveedor servicioProveedor) {
        this.servicioContactoTelefonico = servicioContactoTelefonico;
        this.servicioContacto = servicioContacto;
        this.personaServicio = personaServicio;
        this.servicioProveedor = servicioProveedor;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam(required = false) String idPersona,
                           @RequestParam(required = false) String idProveedor,
                           @RequestParam String telefono,
                           @RequestParam TipoTelefono tipoTelefono,
                           @RequestParam TipoContacto tipoContacto,
                           @RequestParam(required = false) String observacion,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioContactoTelefonico.crearContactoTelefonico(telefono, tipoTelefono, tipoContacto, observacion, vacioANull(idPersona), vacioANull(idProveedor));
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
            ContactoTelefonico contacto = servicioContactoTelefonico.listarContactoTelefonico().stream()
                    .filter(actual -> actual.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new Exception("No existe un contacto telefónico con id: " + id));
            modelo.addAttribute("contactoTelefonico", contacto);
            modelo.addAttribute("contactosTelefonicos", servicioContactoTelefonico.listarContactoTelefonico());
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

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
