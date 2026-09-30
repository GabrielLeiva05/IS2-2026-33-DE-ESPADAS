package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.*;
import com.ejercicioIntegrador.tiendaderopa.model.*;
import com.ejercicioIntegrador.tiendaderopa.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Date;

/**
 * ABM de facturas de proveedores embebido en el dashboard (fragmento fragments/abmFacturaProveedor.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/facturasproveedor) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/factura-proveedor")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class FacturaProveedorAbmControlador {

    private final ServicioFacturaProveedor servicioFacturaProveedor;
    private final ServicioFormaDePago servicioFormaDePago;
    private final ServicioProveedor servicioProveedor;
    private final ServicioOrdenCompraProveedor servicioOrdenCompra;

    public FacturaProveedorAbmControlador(ServicioFacturaProveedor servicioFacturaProveedor, ServicioFormaDePago servicioFormaDePago, ServicioProveedor servicioProveedor, ServicioOrdenCompraProveedor servicioOrdenCompra) {
        this.servicioFacturaProveedor = servicioFacturaProveedor;
        this.servicioFormaDePago = servicioFormaDePago;
        this.servicioProveedor = servicioProveedor;
        this.servicioOrdenCompra = servicioOrdenCompra;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam Long numeroFactura,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFactura,
                           @RequestParam double totalPagado,
                           @RequestParam String idFormaDePago,
                           @RequestParam String idProveedor,
                           @RequestParam String idOrdenCompraProveedor,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioFacturaProveedor.crearFactura(numeroFactura, fechaFactura, totalPagado, idFormaDePago, idProveedor, idOrdenCompraProveedor);
            redirectAttributes.addFlashAttribute("exito", "Factura de proveedor cargada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("facturaProveedor", servicioFacturaProveedor.buscarPorId(id));
            modelo.addAttribute("facturasProveedor", servicioFacturaProveedor.listarTodas());
            modelo.addAttribute("formasDePago", servicioFormaDePago.listarFormaDePago());
            modelo.addAttribute("proveedores", servicioProveedor.listarProveedor());
            modelo.addAttribute("ordenesCompra", servicioOrdenCompra.listarOrdenCompraProveedor());
            return "panel.html";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                           @RequestParam Long numeroFactura,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFactura,
                           @RequestParam double totalPagado,
                           @RequestParam EstadoFactura estadoFactura,
                           @RequestParam String idFormaDePago,
                           @RequestParam String idProveedor,
                           @RequestParam String idOrdenCompraProveedor,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioFacturaProveedor.modificarFactura(id, numeroFactura, fechaFactura, totalPagado, estadoFactura, idFormaDePago, idProveedor, idOrdenCompraProveedor);
            redirectAttributes.addFlashAttribute("exito", "Factura de proveedor modificada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioFacturaProveedor.eliminarFactura(id);
            redirectAttributes.addFlashAttribute("exito", "Factura de proveedor eliminada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
