package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.PersonaServicio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/admin/personas")
public class PersonaControlador {

    private final PersonaServicio servicio;

    public PersonaControlador(PersonaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            return mostrar(model, servicio.buscarPersona(id));
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/personas";
        }
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return mostrar(model, null);
    }

    @PostMapping
    public String crear(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String documento,
            RedirectAttributes redirect
    ) {
        try {
            servicio.crearPersona(nombre, apellido, fechaNacimiento, tipoDocumento, documento);
            redirect.addFlashAttribute("mensaje", "Persona creada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/personas";
    }

    @PostMapping("/{id}")
    public String modificar(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String documento,
            RedirectAttributes redirect
    ) {
        try {
            servicio.modificarPersona(id, nombre, apellido, fechaNacimiento, tipoDocumento, documento);
            redirect.addFlashAttribute("mensaje", "Persona actualizada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/personas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarPersona(id);
            redirect.addFlashAttribute("mensaje", "Persona eliminada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/personas";
    }

    @PostMapping("/{idPersona}/usuario")
    public String asignarUsuario(@PathVariable String idPersona, @RequestParam String usuarioId,
            RedirectAttributes redirect) {
        try {
            servicio.asignarUsuario(idPersona, usuarioId);
            redirect.addFlashAttribute("mensaje", "Usuario asociado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/personas";
    }

    @PostMapping("/{idPersona}/usuario/eliminar")
    public String removerUsuario(@PathVariable String idPersona, RedirectAttributes redirect) {
        try {
            servicio.removerUsuario(idPersona);
            redirect.addFlashAttribute("mensaje", "Usuario desasociado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/personas";
    }

    private String mostrar(Model model, Persona seleccionada) {
        AdminPageSupport.cargar(model, "Personas", "/admin/personas", Persona.class,
                servicio.listarPersona(), java.util.List.of(
                        AdminPageSupport.campo("nombre", "text", true),
                        AdminPageSupport.campo("apellido", "text", true),
                        AdminPageSupport.campo("fechaNacimiento", "date", true),
                        AdminPageSupport.campo("tipoDocumento", "select", true,
                                java.util.Arrays.stream(TipoDocumento.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campo("documento", "text", true)), seleccionada);
        model.addAttribute("asociacionUsuarios", true);
        model.addAttribute("desasociarUsuarios", true);
        return "admin/registros";
    }
}