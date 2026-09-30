package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.PersonaServicio;
import com.ejercicioIntegrador.tiendaderopa.service.UsuarioServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminControlador {

    private final UsuarioServicio servicio;
    private final PersonaServicio personaServicio;

    public UsuarioAdminControlador(UsuarioServicio servicio, PersonaServicio personaServicio) {
        this.servicio = servicio;
        this.personaServicio = personaServicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            return mostrar(model, servicio.buscarPorId(id));
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping
    public String crear(@RequestParam String nombreUsuario, @RequestParam String claveNueva,
            @RequestParam RolUsuario rolUsuario, @RequestParam String personaId, RedirectAttributes redirect) {
        try {
            servicio.crearUsuarioAdmin(nombreUsuario, claveNueva, rolUsuario, personaId);
            redirect.addFlashAttribute("mensaje", "Usuario creado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombreUsuario,
            @RequestParam(required = false) String claveNueva, @RequestParam RolUsuario rolUsuario,
            @RequestParam String personaId, RedirectAttributes redirect) {
        try {
            servicio.modificarUsuarioAdmin(id, nombreUsuario, claveNueva, rolUsuario, personaId);
            redirect.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarUsuario(id);
            redirect.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    private String mostrar(Model model, Usuario seleccionado) {
        AdminPageSupport.cargar(model, "Usuarios", "/admin/usuarios", Usuario.class,
                servicio.listarTodos(), java.util.List.of(
                        AdminPageSupport.campo("nombreUsuario", "email", true),
                        AdminPageSupport.campo("claveNueva", "password", seleccionado == null),
                        AdminPageSupport.campo("rolUsuario", "select", true,
                                java.util.Arrays.stream(RolUsuario.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campoRelacion("personaId", true, AdminPageSupport.mapaOpciones(
                                personaServicio.listarPersona(), Persona::getId,
                                persona -> persona.getNombre() + " " + persona.getApellido()))),
                seleccionado);
        return "admin/registros";
    }
}
