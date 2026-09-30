package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.FormaDePago;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioFacturaProveedor;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioFormaDePago;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProveedor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/admin/facturasproveedor")
public class FacturaProveedorAdminControlador {

    private final ServicioFacturaProveedor servicio;
    private final ServicioFormaDePago formaDePagoServicio;
    private final ServicioProveedor proveedorServicio;
    private final ServicioOrdenCompraProveedor ordenCompraProveedorServicio;

    public FacturaProveedorAdminControlador(ServicioFacturaProveedor servicio,
            ServicioFormaDePago formaDePagoServicio, ServicioProveedor proveedorServicio,
            ServicioOrdenCompraProveedor ordenCompraProveedorServicio) {
        this.servicio = servicio;
        this.formaDePagoServicio = formaDePagoServicio;
        this.proveedorServicio = proveedorServicio;
        this.ordenCompraProveedorServicio = ordenCompraProveedorServicio;
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
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/facturasproveedor";
        }
    }

    @PostMapping
    public String crear(@RequestParam Long numeroFactura,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFactura,
            @RequestParam double totalPagado, @RequestParam String idFormaDePago,
            @RequestParam String idProveedor, @RequestParam String idOrdenCompraProveedor,
            RedirectAttributes redirect) {
        try {
            servicio.crearFactura(numeroFactura, fechaFactura, totalPagado, idFormaDePago, idProveedor,
                    idOrdenCompraProveedor);
            redirect.addFlashAttribute("mensaje", "Factura creada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/facturasproveedor";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam Long numeroFactura,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFactura,
            @RequestParam double totalPagado, @RequestParam EstadoFactura estadoFactura,
            @RequestParam String idFormaDePago, @RequestParam String idProveedor,
            @RequestParam String idOrdenCompraProveedor, RedirectAttributes redirect) {
        try {
            servicio.modificarFactura(id, numeroFactura, fechaFactura, totalPagado, estadoFactura, idFormaDePago,
                    idProveedor, idOrdenCompraProveedor);
            redirect.addFlashAttribute("mensaje", "Factura actualizada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/facturasproveedor";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarFactura(id);
            redirect.addFlashAttribute("mensaje", "Factura eliminada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/facturasproveedor";
    }

    private String mostrar(Model model, FacturaProveedor seleccionada) {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        AdminPageSupport.cargar(model, "Facturas de proveedores", "/admin/facturasproveedor", FacturaProveedor.class,
                servicio.listarTodas(), java.util.List.of(
                        AdminPageSupport.campo("numeroFactura", "number", true),
                        AdminPageSupport.campo("fechaFactura", "date", true),
                        AdminPageSupport.campo("totalPagado", "number", true),
                        AdminPageSupport.campo("estadoFactura", "select", true,
                                java.util.Arrays.stream(EstadoFactura.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campoRelacion("idFormaDePago", true, AdminPageSupport.mapaOpciones(
                                formaDePagoServicio.listarFormaDePagoActivo(), FormaDePago::getId,
                                forma -> forma.getTipoPago() + (forma.getObservacion() != null ? " - " + forma.getObservacion() : ""))),
                        AdminPageSupport.campoRelacion("idProveedor", true, AdminPageSupport.mapaOpciones(
                                proveedorServicio.listarProveedorActivo(), Proveedor::getId, Proveedor::getRazonSocial)),
                        AdminPageSupport.campoRelacion("idOrdenCompraProveedor", true, AdminPageSupport.mapaOpciones(
                                ordenCompraProveedorServicio.listarOrdenCompraProveedor(), OrdenCompraProveedor::getId,
                                orden -> "Orden del " + formatoFecha.format(orden.getFecha()) + " ($" + orden.getTotal() + ")"))),
                seleccionada);
        return "admin/registros";
    }
}
