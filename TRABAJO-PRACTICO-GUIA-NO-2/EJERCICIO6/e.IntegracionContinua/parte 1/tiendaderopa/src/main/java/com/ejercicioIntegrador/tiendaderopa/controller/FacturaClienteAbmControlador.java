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
 * ABM de facturas de clientes embebido en el dashboard (fragmento fragments/abmFacturaCliente.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/facturascliente) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/factura-cliente")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class FacturaClienteAbmControlador {

    private final ServicioFacturaCliente servicioFacturaCliente;
    private final ServicioFormaDePago servicioFormaDePago;

    public FacturaClienteAbmControlador(ServicioFacturaCliente servicioFacturaCliente, ServicioFormaDePago servicioFormaDePago) {
        this.servicioFacturaCliente = servicioFacturaCliente;
        this.servicioFormaDePago = servicioFormaDePago;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam Long numeroFactura,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFactura,
                           @RequestParam double totalPagado,
                           @RequestParam EstadoFactura estadoFactura,
                           @RequestParam String idFormaDePago,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioFacturaCliente.crearFactura(numeroFactura, fechaFactura, totalPagado, estadoFactura, idFormaDePago);
            redirectAttributes.addFlashAttribute("exito", "Factura de cliente cargada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("facturaCliente", servicioFacturaCliente.buscarPorId(id));
            modelo.addAttribute("facturasCliente", servicioFacturaCliente.listarTodas());
            modelo.addAttribute("formasDePago", servicioFormaDePago.listarFormaDePago());
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
                           RedirectAttributes redirectAttributes) {
        try {
            servicioFacturaCliente.modificarFactura(id, numeroFactura, fechaFactura, totalPagado, estadoFactura, idFormaDePago);
            redirectAttributes.addFlashAttribute("exito", "Factura de cliente modificada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioFacturaCliente.eliminarFactura(id);
            redirectAttributes.addFlashAttribute("exito", "Factura de cliente eliminada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
