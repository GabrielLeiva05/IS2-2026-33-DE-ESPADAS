package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Provincia;
import com.ejercicioIntegrador.tiendaderopa.service.DepartamentoServicio;
import com.ejercicioIntegrador.tiendaderopa.service.ProvinciaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departamento")
public class DepartamentoControlador {

    private final DepartamentoServicio departamentoServicio;
    private final ProvinciaServicio provinciaServicio;

    public DepartamentoControlador(DepartamentoServicio departamentoServicio, ProvinciaServicio provinciaServicio) {
        this.departamentoServicio = departamentoServicio;
        this.provinciaServicio = provinciaServicio;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String idProvincia,
                           RedirectAttributes redirectAttributes) {
        try {
            Provincia provincia = provinciaServicio.buscarPorId(idProvincia);
            departamentoServicio.crearDepartamento(nombre, provincia);
            redirectAttributes.addFlashAttribute("exito", "Departamento cargado correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos del departamento a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo) {
        try {
            Departamento departamento = departamentoServicio.buscarPorId(id);
            modelo.addAttribute("departamento", departamento);
            modelo.addAttribute("departamentos", departamentoServicio.listarTodos());
            modelo.addAttribute("provincias", provinciaServicio.listarTodas());
            modelo.addAttribute("provincia", null);
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
                            @RequestParam String idProvincia,
                            RedirectAttributes redirectAttributes) {
        try {
            Provincia provincia = provinciaServicio.buscarPorId(idProvincia);
            departamentoServicio.modificarDepartamento(id, nombre, provincia);
            redirectAttributes.addFlashAttribute("exito", "Departamento modificado correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            departamentoServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Departamento eliminado correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}