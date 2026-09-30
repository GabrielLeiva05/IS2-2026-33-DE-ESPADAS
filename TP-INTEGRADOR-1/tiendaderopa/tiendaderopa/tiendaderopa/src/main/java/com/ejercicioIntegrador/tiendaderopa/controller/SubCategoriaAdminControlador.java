package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Categoria;
import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioCategoria;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioSubCategoria;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/subcategorias")
public class SubCategoriaAdminControlador {

    private final ServicioSubCategoria servicio;
    private final ServicioCategoria categoriaServicio;

    public SubCategoriaAdminControlador(ServicioSubCategoria servicio, ServicioCategoria categoriaServicio) {
        this.servicio = servicio;
        this.categoriaServicio = categoriaServicio;
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
            redirect.addFlashAttribute("error", "No se encontró la subcategoría solicitada.");
            return "redirect:/admin/subcategorias";
        }
    }

    @PostMapping
    public String crear(@RequestParam String nombre, @RequestParam String categoriaId, RedirectAttributes redirect) {
        try {
            Categoria categoria = categoriaServicio.findById(categoriaId);
            servicio.crear(nombre, categoria);
            redirect.addFlashAttribute("mensaje", "Subcategoría creada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/subcategorias";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombre,
            @RequestParam String categoriaId, @RequestParam(defaultValue = "false") boolean activo,
            RedirectAttributes redirect) {
        try {
            Categoria categoria = categoriaServicio.findById(categoriaId);
            servicio.modificar(id, nombre, categoria, activo);
            redirect.addFlashAttribute("mensaje", "Subcategoría actualizada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/subcategorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Subcategoría desactivada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/subcategorias";
    }

    private String mostrar(Model model, SubCategoria seleccionada) throws Exception {
        AdminPageSupport.cargar(model, "Subcategorías", "/admin/subcategorias", SubCategoria.class,
                servicio.findAll(), java.util.List.of(
                        AdminPageSupport.campo("nombre", "text", true),
                        AdminPageSupport.campoRelacion("categoriaId", true, AdminPageSupport.mapaOpciones(
                                categoriaServicio.findAll(), Categoria::getId, Categoria::getNombre)),
                        AdminPageSupport.campo("activo", "checkbox", false)),
                seleccionada);
        return "admin/registros";
    }
}
