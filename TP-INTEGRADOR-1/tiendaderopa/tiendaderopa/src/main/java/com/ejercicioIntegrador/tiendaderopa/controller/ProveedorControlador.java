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
@RequestMapping("/proveedor")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class ProveedorControlador {

    private final ServicioProveedor servicioProveedor;

    public ProveedorControlador(ServicioProveedor servicioProveedor) {
        this.servicioProveedor = servicioProveedor;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String razonSocial,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioProveedor.crearProveedor(razonSocial);
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
            modelo.addAttribute("proveedor", servicioProveedor.buscarPorId(id));
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
            servicioProveedor.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Proveedor eliminado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
