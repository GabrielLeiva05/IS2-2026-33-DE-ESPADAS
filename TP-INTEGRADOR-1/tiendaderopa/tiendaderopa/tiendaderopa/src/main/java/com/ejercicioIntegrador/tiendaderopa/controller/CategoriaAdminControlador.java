package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Categoria;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioCategoria;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaAdminControlador {

    private final ServicioCategoria servicio;

    public CategoriaAdminControlador(ServicioCategoria servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) throws Exception {
        return mostrar(model, null);
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) throws Exception {
        return mostrar(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) throws Exception {
        try {
            return mostrar(model, servicio.findById(id));
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se encontró la categoría solicitada.");
            return "redirect:/admin/categorias";
        }
    }

    @PostMapping
    public String crear(@RequestParam String nombre, RedirectAttributes redirect) {
        try {
            servicio.crear(nombre);
            redirect.addFlashAttribute("mensaje", "Categoría creada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombre,
            @RequestParam(defaultValue = "false") boolean activo, RedirectAttributes redirect) {
        try {
            servicio.modificar(id, nombre, activo);
            redirect.addFlashAttribute("mensaje", "Categoría actualizada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Categoría desactivada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    private String mostrar(Model model, Categoria seleccionada) throws Exception {
        AdminPageSupport.cargar(model, "Categorías", "/admin/categorias", Categoria.class,
                servicio.findAll(), java.util.List.of(
                        AdminPageSupport.campo("nombre", "text", true),
                        AdminPageSupport.campo("activo", "checkbox", false)),
                seleccionada);
        return "admin/registros";
    }
}
