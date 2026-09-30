package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioDetalleCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/detalles-compra")
public class ControladorDetalleCompra {

    @Autowired
    private ServicioDetalleCompra servicioDetalleCompra;

    @GetMapping
    public String listarTodos(Model model) {
        String vista = mostrar(model, servicioDetalleCompra.listarTodos());
        model.addAttribute("permitirFiltroId", true);
        model.addAttribute("rutaFiltroId", "/admin/detalles-compra/orden");
        model.addAttribute("nombreFiltroId", "ordenId");
        return vista;
    }

    @GetMapping("/{id}")
    public String obtenerPorId(@PathVariable String id, Model model) {
        return mostrar(model, java.util.List.of(servicioDetalleCompra.buscarPorId(id)));
    }

    @GetMapping("/orden/{ordenId}")
    public String listarPorOrden(@PathVariable String ordenId, Model model) {
        return mostrar(model, servicioDetalleCompra.listarPorOrden(ordenId));
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        servicioDetalleCompra.eliminarDetalleCompra(id);
        redirect.addFlashAttribute("mensaje", "Detalle de compra eliminado correctamente.");
        return "redirect:/admin/detalles-compra";
    }

    private String mostrar(Model model, java.util.Collection<DetalleCompra> detalles) {
        AdminPageSupport.cargar(model, "Detalles de compra", "/admin/detalles-compra", DetalleCompra.class,
                detalles, java.util.List.of(), null);
        model.addAttribute("permitirCrear", false);
        model.addAttribute("permitirEditar", false);
        model.addAttribute("permitirFiltroId", true);
        model.addAttribute("rutaFiltroId", "/admin/detalles-compra/orden");
        model.addAttribute("nombreFiltroId", "ordenId");
        return "admin/registros";
    }
}