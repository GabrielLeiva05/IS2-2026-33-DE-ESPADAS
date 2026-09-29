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
@RequestMapping("/subcategoria")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class SubCategoriaControlador {

    private final ServicioSubCategoria servicioSubCategoria;
    private final ServicioCategoria servicioCategoria;

    public SubCategoriaControlador(ServicioSubCategoria servicioSubCategoria, ServicioCategoria servicioCategoria) {
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
            servicioSubCategoria.crearSubCategoria(nombre, categoria);
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
            modelo.addAttribute("subcategorias", servicioSubCategoria.listarTodas());
            modelo.addAttribute("categorias", servicioCategoria.listarTodas());
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
                           RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = servicioCategoria.findById(idCategoria);
            servicioSubCategoria.modificarSubCategoria(id, nombre, categoria);
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
            servicioSubCategoria.deleteById(id);
            redirectAttributes.addFlashAttribute("exito", "Estado de la subcategoría actualizado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
