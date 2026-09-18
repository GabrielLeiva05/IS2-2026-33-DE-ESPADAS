package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Pais;
import com.ejercicioIntegrador.tiendaderopa.service.PaisServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pais") // URL base: http://localhost:8080/pais
public class PaisControlador {

    private final PaisServicio paisServicio;

    public PaisControlador(PaisServicio paisServicio) {
        this.paisServicio = paisServicio;
    }

    // 1. LISTAR Y FORMULARIO DE CREACIÓN (GET)
    @GetMapping("/registrar")
    public String registrar(ModelMap modelo) {
        List<Pais> paises = paisServicio.listarTodos();
        modelo.addAttribute("paises", paises);
        return "pais_admin.html"; // Vista combinada (Listado + Formulario de alta)
    }

    // 2. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre, RedirectAttributes redirectAttributes) {
        try {
            paisServicio.crearPais(nombre);
            redirectAttributes.addFlashAttribute("exito", "País cargado correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/pais/registrar";
    }

    // 3. VISTA MODIFICAR (GET)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo) {
        try {
            Pais pais = paisServicio.buscarPorId(id);
            modelo.put("pais", pais);
            return "pais_modificar.html";
        } catch (MiException ex) {
            modelo.put("error", ex.getMessage());
            return "redirect:/pais/registrar";
        }
    }

    // 4. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombre, RedirectAttributes redirectAttributes) {
        try {
            paisServicio.modificarPais(id, nombre);
            redirectAttributes.addFlashAttribute("exito", "País modificado correctamente.");
            return "redirect:/pais/registrar";
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/pais/modificar/" + id;
        }
    }

    // 5. ELIMINAR (GET o POST)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            paisServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "País eliminado correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/pais/registrar";
    }
}