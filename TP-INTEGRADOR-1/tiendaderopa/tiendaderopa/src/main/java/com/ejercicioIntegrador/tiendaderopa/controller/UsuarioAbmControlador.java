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
 * ABM de usuarios embebido en el dashboard (fragmento fragments/abmUsuario.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/usuarios) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/usuario")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class UsuarioAbmControlador {

    private final UsuarioServicio usuarioServicio;
    private final PersonaServicio personaServicio;

    public UsuarioAbmControlador(UsuarioServicio usuarioServicio, PersonaServicio personaServicio) {
        this.usuarioServicio = usuarioServicio;
        this.personaServicio = personaServicio;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String idPersona,
                           @RequestParam String nombreUsuario,
                           @RequestParam RolUsuario rolUsuario,
                           @RequestParam String clave,
                           RedirectAttributes redirectAttributes) {
        try {
            usuarioServicio.crearUsuarioAdmin(nombreUsuario, clave, rolUsuario, idPersona);
            redirectAttributes.addFlashAttribute("exito", "Usuario cargado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("usuario", usuarioServicio.buscarPorId(id));
            modelo.addAttribute("usuarios", usuarioServicio.listarTodos());
            modelo.addAttribute("personas", personaServicio.listarPersona());
            return "panel.html";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                           @RequestParam String idPersona,
                           @RequestParam String nombreUsuario,
                           @RequestParam RolUsuario rolUsuario,
                           @RequestParam(required = false) String clave,
                           RedirectAttributes redirectAttributes) {
        try {
            usuarioServicio.modificarUsuarioAdmin(id, nombreUsuario, clave, rolUsuario, idPersona);
            redirectAttributes.addFlashAttribute("exito", "Usuario modificado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Usuario logueado = (Usuario) session.getAttribute("usuariosession");
            if (logueado != null && id.equals(logueado.getId())) {
                throw new Exception("No podés eliminar el usuario con el que iniciaste sesión.");
            }
            usuarioServicio.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("exito", "Usuario eliminado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
