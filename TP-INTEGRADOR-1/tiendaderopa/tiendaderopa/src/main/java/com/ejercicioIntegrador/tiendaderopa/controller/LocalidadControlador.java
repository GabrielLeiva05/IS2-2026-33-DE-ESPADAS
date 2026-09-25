package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.service.DepartamentoServicio;
import com.ejercicioIntegrador.tiendaderopa.service.LocalidadServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/localidad")
public class LocalidadControlador {

    private final LocalidadServicio localidadServicio;
    private final DepartamentoServicio departamentoServicio;

    public LocalidadControlador(LocalidadServicio localidadServicio, DepartamentoServicio departamentoServicio) {
        this.localidadServicio = localidadServicio;
        this.departamentoServicio = departamentoServicio;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String codigoPostal, // <-- Recibir CP
                           @RequestParam String idDepartamento,
                           RedirectAttributes redirectAttributes) {
        try {
            Departamento departamento = departamentoServicio.buscarPorId(idDepartamento);
            localidadServicio.crearLocalidad(nombre, codigoPostal, departamento); // <-- Pasar CP
            redirectAttributes.addFlashAttribute("exito", "Localidad cargada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo) {
        try {
            Localidad localidad = localidadServicio.buscarPorId(id);
            modelo.addAttribute("localidad", localidad);
            modelo.addAttribute("localidades", localidadServicio.listarTodas());
            modelo.addAttribute("departamentos", departamentoServicio.listarTodos());
            modelo.addAttribute("departamento", null);
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
                            @RequestParam String codigoPostal, // <-- Recibir CP
                            @RequestParam String idDepartamento,
                            RedirectAttributes redirectAttributes) {
        try {
            Departamento departamento = departamentoServicio.buscarPorId(idDepartamento);
            localidadServicio.modificarLocalidad(id, nombre, codigoPostal, departamento); // <-- Pasar CP
            redirectAttributes.addFlashAttribute("exito", "Localidad modificada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            localidadServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Localidad eliminada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}