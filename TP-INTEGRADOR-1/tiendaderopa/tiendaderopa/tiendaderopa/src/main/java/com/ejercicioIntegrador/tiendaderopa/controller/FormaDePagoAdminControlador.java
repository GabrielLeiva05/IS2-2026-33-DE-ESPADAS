package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.FormaDePago;
import com.ejercicioIntegrador.tiendaderopa.model.TipoPago;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioFormaDePago;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/formasdepago")
public class FormaDePagoAdminControlador {

    private final ServicioFormaDePago servicio;

    public FormaDePagoAdminControlador(ServicioFormaDePago servicio) {
        this.servicio = servicio;
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
            return mostrar(model, servicio.buscarFormaDePago(id));
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/formasdepago";
        }
    }

    @PostMapping
    public String crear(@RequestParam TipoPago tipoPago, @RequestParam(required = false) String observacion,
            RedirectAttributes redirect) {
        try {
            servicio.crearFormaDePago(tipoPago, observacion);
            redirect.addFlashAttribute("mensaje", "Forma de pago creada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/formasdepago";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam TipoPago tipoPago,
            @RequestParam(required = false) String observacion, RedirectAttributes redirect) {
        try {
            servicio.modificarFormaDePago(id, tipoPago, observacion);
            redirect.addFlashAttribute("mensaje", "Forma de pago actualizada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/formasdepago";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarFormaDePago(id);
            redirect.addFlashAttribute("mensaje", "Forma de pago eliminada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/formasdepago";
    }

    private String mostrar(Model model, FormaDePago seleccionada) {
        AdminPageSupport.cargar(model, "Formas de pago", "/admin/formasdepago", FormaDePago.class,
                servicio.listarFormaDePago(), java.util.List.of(
                        AdminPageSupport.campo("tipoPago", "select", true,
                                java.util.Arrays.stream(TipoPago.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campo("observacion", "text", false)),
                seleccionada);
        return "admin/registros";
    }
}
