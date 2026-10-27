package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Pais;
import com.ejercicioIntegrador.tiendaderopa.model.Provincia;
import com.ejercicioIntegrador.tiendaderopa.service.PaisServicio;
import com.ejercicioIntegrador.tiendaderopa.service.ProvinciaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/provincia")
public class ProvinciaControlador {

    private final ProvinciaServicio provinciaServicio;
    private final PaisServicio paisServicio;

    public ProvinciaControlador(ProvinciaServicio provinciaServicio, PaisServicio paisServicio) {
        this.provinciaServicio = provinciaServicio;
        this.paisServicio = paisServicio;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String idPais,
                           RedirectAttributes redirectAttributes) {
        try {
            Pais pais = paisServicio.buscarPorId(idPais);
            provinciaServicio.crearProvincia(nombre, pais);
            redirectAttributes.addFlashAttribute("exito", "Provincia cargada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos de la provincia a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo) {
        try {
            Provincia provincia = provinciaServicio.buscarPorId(id);
            modelo.addAttribute("provincia", provincia);
            modelo.addAttribute("provincias", provinciaServicio.listarTodas());
            modelo.addAttribute("paises", paisServicio.listarTodos());
            modelo.addAttribute("pais", null);
            return "panel.html";
        } catch (MiException ex) {
            modelo.addAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                            @RequestParam String nombre,
                            @RequestParam String idPais,
                            RedirectAttributes redirectAttributes) {
        try {
            Pais pais = paisServicio.buscarPorId(idPais);
            // Si creaste un método modificarProvincia(id, nombre, pais) en tu servicio:
            provinciaServicio.modificarProvincia(id, nombre, pais);
            redirectAttributes.addFlashAttribute("exito", "Provincia modificada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            provinciaServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Provincia eliminada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}