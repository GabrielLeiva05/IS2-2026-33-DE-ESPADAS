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
 * ABM de categorías embebido en el dashboard (fragmento fragments/abmCategoria.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/categorias) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/categoria")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class CategoriaAbmControlador {

    private final ServicioCategoria servicioCategoria;

    public CategoriaAbmControlador(ServicioCategoria servicioCategoria) {
        this.servicioCategoria = servicioCategoria;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioCategoria.crear(nombre);
            redirectAttributes.addFlashAttribute("exito", "Categoría cargada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("categoria", servicioCategoria.findById(id));
            modelo.addAttribute("categorias", servicioCategoria.findAll());
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
                           @RequestParam(defaultValue = "false") boolean activo,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioCategoria.modificar(id, nombre, activo);
            redirectAttributes.addFlashAttribute("exito", "Categoría modificada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioCategoria.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Categoría desactivada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
