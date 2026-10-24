package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.DepartamentoServicio;
import com.ejercicioIntegrador.tiendaderopa.service.LocalidadServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/localidades")
public class LocalidadAdminControlador {

    private final LocalidadServicio servicio;
    private final DepartamentoServicio departamentoServicio;

    public LocalidadAdminControlador(LocalidadServicio servicio, DepartamentoServicio departamentoServicio) {
        this.servicio = servicio;
        this.departamentoServicio = departamentoServicio;
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
            return "redirect:/admin/localidades";
        }
    }

    @PostMapping
    public String crear(@RequestParam String nombre, @RequestParam(required = false) String codigoPostal,
            @RequestParam String departamentoId, RedirectAttributes redirect) {
        try {
            Departamento departamento = departamentoServicio.buscarPorId(departamentoId);
            servicio.crearLocalidad(nombre, codigoPostal, departamento);
            redirect.addFlashAttribute("mensaje", "Localidad creada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/localidades";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombre,
            @RequestParam(required = false) String codigoPostal, @RequestParam String departamentoId,
            RedirectAttributes redirect) {
        try {
            Departamento departamento = departamentoServicio.buscarPorId(departamentoId);
            servicio.modificarLocalidad(id, nombre, codigoPostal, departamento);
            redirect.addFlashAttribute("mensaje", "Localidad actualizada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/localidades";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Localidad eliminada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/localidades";
    }

    private String mostrar(Model model, Localidad seleccionada) {
        AdminPageSupport.cargar(model, "Localidades", "/admin/localidades", Localidad.class,
                servicio.listarTodas(), java.util.List.of(
                        AdminPageSupport.campo("nombre", "text", true),
                        AdminPageSupport.campo("codigoPostal", "text", true),
                        AdminPageSupport.campoRelacion("departamentoId", true, AdminPageSupport.mapaOpciones(
                                departamentoServicio.listarTodos(), Departamento::getId, Departamento::getNombre))),
                seleccionada);
        return "admin/registros";
    }
}
