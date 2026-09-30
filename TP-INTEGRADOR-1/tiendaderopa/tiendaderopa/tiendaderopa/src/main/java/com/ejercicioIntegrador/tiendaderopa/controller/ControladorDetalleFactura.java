package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioDetalleFactura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/detalles-factura")
public class ControladorDetalleFactura {

    @Autowired
    private ServicioDetalleFactura servicioDetalleFactura;

    @GetMapping
    public String listarTodos(Model model) {
        return mostrar(model, servicioDetalleFactura.listarTodos(), null);
    }

    @GetMapping("/{id}")
    public String obtenerPorId(@PathVariable String id, Model model) throws Exception {
        return mostrar(model, java.util.List.of(servicioDetalleFactura.buscarPorId(id)), null);
    }

    @GetMapping("/factura/{facturaId}")
    public String listarPorFactura(@PathVariable String facturaId, Model model) {
        return mostrar(model, servicioDetalleFactura.listarPorFactura(facturaId), null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model) throws Exception {
        DetalleFactura detalle = servicioDetalleFactura.buscarPorId(id);
        String vista = mostrar(model, servicioDetalleFactura.listarTodos(), detalle);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> valores = (java.util.Map<String, Object>) model.getAttribute("valores");
        valores.put("codigoProducto", detalle.getProducto().getCodigo());
        return vista;
    }

    @PostMapping("/{id}")
    public String modificarProducto(
            @PathVariable String id,
            @RequestParam String codigoProducto,
            RedirectAttributes redirect) throws Exception {
        servicioDetalleFactura.modificarDetalleFactura(id, codigoProducto);
        redirect.addFlashAttribute("mensaje", "Detalle de factura actualizado correctamente.");
        return "redirect:/admin/detalles-factura";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) throws Exception {
        servicioDetalleFactura.eliminarDetalleFactura(id);
        redirect.addFlashAttribute("mensaje", "Detalle de factura eliminado correctamente.");
        return "redirect:/admin/detalles-factura";
    }

    private String mostrar(Model model, java.util.Collection<DetalleFactura> detalles, DetalleFactura seleccionado) {
        AdminPageSupport.cargar(model, "Detalles de factura", "/admin/detalles-factura", DetalleFactura.class,
                detalles, java.util.List.of(AdminPageSupport.campo("codigoProducto", "text", true)), seleccionado);
        model.addAttribute("permitirCrear", false);
        model.addAttribute("permitirFiltroId", true);
        model.addAttribute("rutaFiltroId", "/admin/detalles-factura/factura");
        model.addAttribute("nombreFiltroId", "facturaId");
        if (seleccionado == null) {
            model.addAttribute("permitirEditar", false);
        }
        return "admin/registros";
    }
}