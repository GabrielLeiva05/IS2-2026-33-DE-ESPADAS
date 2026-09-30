package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Nacionalidad;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.NacionalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/nacionalidades")
@RequiredArgsConstructor
public class NacionalidadController {

    private final NacionalidadService service;

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null, service.listarNacionalidad());
    }

    @GetMapping("/activas")
    public String listarActivas(Model model) {
        return mostrar(model, null, service.listarNacionalidadActiva());
    }

    @GetMapping("/buscar")
    public String buscarPorNombre(@RequestParam String nombre, Model model) {
        model.addAttribute("permitirBuscarNombre", true);
        return mostrar(model, null, java.util.List.of(service.buscarNacionalidadPorNombre(nombre)));
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model) {
        return mostrar(model, service.buscarNacionalidad(id), service.listarNacionalidad());
    }

    @PostMapping
    public String crear(@RequestParam String nombre, RedirectAttributes redirect) {
        try {
            service.crearNacionalidad(nombre);
            redirect.addFlashAttribute("mensaje", "Nacionalidad creada correctamente.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/nacionalidades";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombre, RedirectAttributes redirect) {
        try {
            service.modificarNacionalidad(id, nombre);
            redirect.addFlashAttribute("mensaje", "Nacionalidad actualizada correctamente.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/nacionalidades";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            service.eliminarNacionalidad(id);
            redirect.addFlashAttribute("mensaje", "Nacionalidad eliminada correctamente.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/nacionalidades";
    }

    private String mostrar(Model model, Nacionalidad seleccionada, java.util.Collection<Nacionalidad> nacionalidades) {
        AdminPageSupport.cargar(model, "Nacionalidades", "/admin/nacionalidades", Nacionalidad.class,
                nacionalidades, java.util.List.of(
                        AdminPageSupport.campo("nombre", "text", true)), seleccionada);
        model.addAttribute("permitirBuscarNombre", true);
        model.addAttribute("permitirMostrarActivos", true);
        return "admin/registros";
    }
}