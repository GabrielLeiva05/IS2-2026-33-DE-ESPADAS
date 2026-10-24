package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioGestionOrdenCompraProveedor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ControladorOrdenCompraProveedor {

    private static final String RUTA = "/admin/ordenes-compra-proveedor";

    private final ServicioGestionOrdenCompraProveedor servicio;

    public ControladorOrdenCompraProveedor(ServicioGestionOrdenCompraProveedor servicio) {
        this.servicio = servicio;
    }

    @GetMapping(RUTA)
    public String listar(Model model) {
        servicio.cargarListado(model);
        return "admin/registros";
    }

    @GetMapping(RUTA + "/nuevo")
    public String nuevo(Model model) {
        servicio.cargarFormularioNuevo(model);
        return "admin/registros";
    }

    @PostMapping(RUTA + "/iniciar")
    public String iniciar(@RequestParam String idProveedor, RedirectAttributes redirect) {
        try {
            return "redirect:" + RUTA + "/" + servicio.iniciarOrden(idProveedor);
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:" + RUTA + "/nuevo";
        }
    }

    @GetMapping(RUTA + "/{id}")
    public String ver(@PathVariable String id, Model model) throws Exception {
        servicio.cargarDetalle(model, id);
        return "admin/registros";
    }

    @PostMapping(RUTA + "/{id}/detalles")
    public String agregarDetalle(@PathVariable String id, @RequestParam String productoId,
                                 @RequestParam int cantidad, @RequestParam double precioCompra, RedirectAttributes redirect) {
        try {
            servicio.agregarDetalle(id, productoId, cantidad, precioCompra);
            redirect.addFlashAttribute("mensaje", "Producto agregado a la orden.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + RUTA + "/" + id;
    }

    @PostMapping(RUTA + "/{id}/confirmar")
    public String confirmar(@PathVariable String id, @RequestParam String idFormaDePago, RedirectAttributes redirect) {
        try {
            servicio.confirmarOrden(id, idFormaDePago);
            redirect.addFlashAttribute("mensaje", "Orden confirmada y factura generada.");
            return "redirect:" + RUTA;
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:" + RUTA + "/" + id;
        }
    }

    @PostMapping(RUTA + "/{id}/estado")
    public String cambiarEstado(@PathVariable String id, @RequestParam EstadoOrdenCompraProveedor estado,
                                RedirectAttributes redirect) {
        try {
            servicio.cambiarEstado(id, estado);
            redirect.addFlashAttribute("mensaje", "Estado actualizado.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + RUTA;
    }
}