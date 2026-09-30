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
 * ABM de subcategorías embebido en el dashboard (fragmento fragments/abmSubCategoria.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/subcategorias) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/subcategoria")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class SubCategoriaAbmControlador {

    private final ServicioSubCategoria servicioSubCategoria;
    private final ServicioCategoria servicioCategoria;

    public SubCategoriaAbmControlador(ServicioSubCategoria servicioSubCategoria, ServicioCategoria servicioCategoria) {
        this.servicioSubCategoria = servicioSubCategoria;
        this.servicioCategoria = servicioCategoria;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String idCategoria,
                           RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = servicioCategoria.findById(idCategoria);
            servicioSubCategoria.crear(nombre, categoria);
            redirectAttributes.addFlashAttribute("exito", "Subcategoría cargada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("subcategoria", servicioSubCategoria.findById(id));
            modelo.addAttribute("subcategorias", servicioSubCategoria.findAll());
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
                           @RequestParam String idCategoria,
                           @RequestParam(defaultValue = "false") boolean activo,
                           RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = servicioCategoria.findById(idCategoria);
            servicioSubCategoria.modificar(id, nombre, categoria, activo);
            redirectAttributes.addFlashAttribute("exito", "Subcategoría modificada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioSubCategoria.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Subcategoría desactivada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
