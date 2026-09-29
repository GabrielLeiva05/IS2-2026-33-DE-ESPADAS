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

@Controller
@RequestMapping("/factura-proveedor")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class FacturaProveedorControlador {

    private final ServicioFacturaProveedor servicioFacturaProveedor;
    private final ServicioFactura servicioFactura;
    private final ServicioFormaDePago servicioFormaDePago;
    private final ServicioProveedor servicioProveedor;

    public FacturaProveedorControlador(ServicioFacturaProveedor servicioFacturaProveedor, ServicioFactura servicioFactura, ServicioFormaDePago servicioFormaDePago, ServicioProveedor servicioProveedor) {
        this.servicioFacturaProveedor = servicioFacturaProveedor;
        this.servicioFactura = servicioFactura;
        this.servicioFormaDePago = servicioFormaDePago;
        this.servicioProveedor = servicioProveedor;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam Long numeroFactura,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFactura,
                           @RequestParam double totalPagado,
                           @RequestParam EstadoFactura estadoFactura,
                           @RequestParam String idFormaDePago,
                           @RequestParam String idProveedor,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioFacturaProveedor.crearFactura(numeroFactura, fechaFactura, totalPagado, estadoFactura, idFormaDePago, idProveedor);
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
            modelo.addAttribute("facturaProveedor", servicioFacturaProveedor.buscarFactura(id));
            modelo.addAttribute("facturasProveedor", servicioFacturaProveedor.listarTodas());
            modelo.addAttribute("formasDePago", servicioFormaDePago.listarFormaDePago());
            modelo.addAttribute("proveedores", servicioProveedor.listarTodos());
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
                           RedirectAttributes redirectAttributes) {
        try {
            servicioFacturaProveedor.modificarFactura(id, numeroFactura, fechaFactura, totalPagado, estadoFactura, idFormaDePago, idProveedor);
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
            servicioFactura.eliminarFactura(id);
            redirectAttributes.addFlashAttribute("exito", "Factura de proveedor eliminada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
