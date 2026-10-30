package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Provincia;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.DepartamentoServicio;
import com.ejercicioIntegrador.tiendaderopa.service.ProvinciaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/departamentos")
public class DepartamentoAdminControlador {

    private final DepartamentoServicio servicio;
    private final ProvinciaServicio provinciaServicio;

    public DepartamentoAdminControlador(DepartamentoServicio servicio, ProvinciaServicio provinciaServicio) {
        this.servicio = servicio;
        this.provinciaServicio = provinciaServicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            return mostrar(model, servicio.buscarPorId(id));
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/departamentos";
        }
    }

    @PostMapping
    public String crear(@RequestParam String nombre, @RequestParam String provinciaId, RedirectAttributes redirect) {
        try {
            Provincia provincia = provinciaServicio.buscarPorId(provinciaId);
            servicio.crearDepartamento(nombre, provincia);
            redirect.addFlashAttribute("mensaje", "Departamento creado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/departamentos";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombre,
            @RequestParam String provinciaId, RedirectAttributes redirect) {
        try {
            Provincia provincia = provinciaServicio.buscarPorId(provinciaId);
            servicio.modificarDepartamento(id, nombre, provincia);
            redirect.addFlashAttribute("mensaje", "Departamento actualizado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/departamentos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Departamento eliminado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/departamentos";
    }

    private String mostrar(Model model, Departamento seleccionado) {
        AdminPageSupport.cargar(model, "Departamentos", "/admin/departamentos", Departamento.class,
                servicio.listarTodos(), java.util.List.of(
                        AdminPageSupport.campo("nombre", "text", true),
                        AdminPageSupport.campoRelacion("provinciaId", true, AdminPageSupport.mapaOpciones(
                                provinciaServicio.listarTodas(), Provincia::getId, Provincia::getNombre))),
                seleccionado);
        return "admin/registros";
    }
}
