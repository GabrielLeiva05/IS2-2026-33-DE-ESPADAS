package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.dto.ItemCompraDTO;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
public class ControladorOrdenCompraProveedor {

    @Autowired private ServicioOrdenCompraProveedor svcOrdenCompraProveedor;
    @Autowired private ServicioFacturaProveedor svcFacturaProveedor;
    @Autowired private ServicioFactura svcFactura;
    @Autowired private ServicioProveedor svcProveedor;
    @Autowired private ServicioProducto svcProducto;

    @GetMapping("/ordenesCompraProveedor")
    public String listar(Model model) {
        try {
            model.addAttribute("ordenes", svcOrdenCompraProveedor.listarOrdenCompraProveedor());
            return "views/ordenesCompraProveedor/lista";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/ordenesCompraProveedor/generar")
    public String formulario(Model model) {
        model.addAttribute("proveedores", svcProveedor.listarProveedorActivo());
        model.addAttribute("productos", svcProducto.listarProductoActivo());
        return "views/ordenesCompraProveedor/formulario";
    }

    /**
     * items llega armado desde un formulario Thymeleaf con inputs
     * indexados (items[0].idProducto, items[0].cantidad, etc.) — Spring
     * los junta solo en el List<ItemCompraDTO> gracias al binding por
     * índice, sin necesidad de JSON ni JS extra.
     */
    @PostMapping("/ordenesCompraProveedor/generar")
    public String generar(
            @RequestParam String idProveedor,
            @ModelAttribute List<ItemCompraDTO> items,
            @RequestParam Long numeroFactura,
            @RequestParam String idFormaDePago,
            Model model) {
        try {
            // Paso 1: la orden (con sus DetalleOrdenCompraProveedor)
            OrdenCompraProveedor orden = svcOrdenCompraProveedor.crearOrdenCompraProveedor(idProveedor, items);

            // Paso 2: la factura, apoyada en la orden recién creada.
            // Esta secuencia vive ACÁ (no en ningún Service) para evitar
            // el ciclo ServicioOrdenCompraProveedor <-> ServicioFacturaProveedor.
            svcFacturaProveedor.crearFactura(numeroFactura, new Date(), orden.getTotal(),
                    idFormaDePago, idProveedor, orden.getId());

            return "redirect:/ordenesCompraProveedor";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/ordenesCompraProveedor/entregar/{id}")
    public String marcarEntregada(@PathVariable String id, Model model) {
        try {
            svcOrdenCompraProveedor.marcarComoEntregada(id);
            // A partir de acá, factura.puedeGenerarStock() empieza a
            // devolver true para esta orden. Generar los movimientos de
            // Stock (recorriendo el detalle de la Factura asociada) se
            // conecta en el flujo de Factura/Stock, no en este método.
            return "redirect:/ordenesCompraProveedor";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/ordenesCompraProveedor/anular/{id}")
    public String anular(@PathVariable String id, Model model) {
        try {
            FacturaProveedor factura = svcFacturaProveedor.buscarPorOrdenCompra(id);
            svcFactura.anularFactura(factura.getId()); // valida que no esté pagada
            svcOrdenCompraProveedor.anularOrdenCompraProveedor(id);
            return "redirect:/ordenesCompraProveedor";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
