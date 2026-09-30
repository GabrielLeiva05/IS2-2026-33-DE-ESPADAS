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
 * ABM de proveedores embebido en el dashboard (fragmento fragments/abmProveedor.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/proveedores) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/proveedor")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ProveedorAbmControlador {

    private final ServicioProveedor servicioProveedor;
    private final ServicioRegistroProveedor servicioRegistroProveedor;

    public ProveedorAbmControlador(ServicioProveedor servicioProveedor, ServicioRegistroProveedor servicioRegistroProveedor) {
        this.servicioProveedor = servicioProveedor;
        this.servicioRegistroProveedor = servicioRegistroProveedor;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String razonSocial,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String telefonoFijo,
                           @RequestParam(required = false) String telefonoCelular,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioRegistroProveedor.registrarProveedor(razonSocial, email, telefonoFijo, telefonoCelular);
            redirectAttributes.addFlashAttribute("exito", "Proveedor cargado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("proveedor", servicioProveedor.buscarProveedor(id));
            modelo.addAttribute("proveedores", servicioProveedor.listarProveedor());
            return "panel.html";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                           @RequestParam String razonSocial,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioProveedor.modificarProveedor(id, razonSocial);
            redirectAttributes.addFlashAttribute("exito", "Proveedor modificado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioProveedor.eliminarProveedor(id);
            redirectAttributes.addFlashAttribute("exito", "Proveedor eliminado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
