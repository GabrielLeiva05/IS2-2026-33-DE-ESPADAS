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
@RequestMapping("/forma-de-pago")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class FormaDePagoControlador {

    private final ServicioFormaDePago servicioFormaDePago;

    public FormaDePagoControlador(ServicioFormaDePago servicioFormaDePago) {
        this.servicioFormaDePago = servicioFormaDePago;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam TipoPago tipoPago,
                           @RequestParam(required = false) String observacion,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioFormaDePago.crearFormaDePago(tipoPago, observacion);
            redirectAttributes.addFlashAttribute("exito", "Forma de pago cargada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("formaDePago", servicioFormaDePago.buscarFormaDePago(id));
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
                           @RequestParam TipoPago tipoPago,
                           @RequestParam(required = false) String observacion,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioFormaDePago.modificarFormaDePago(id, tipoPago, observacion);
            redirectAttributes.addFlashAttribute("exito", "Forma de pago modificada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioFormaDePago.eliminarFormaDePago(id);
            redirectAttributes.addFlashAttribute("exito", "Forma de pago eliminada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
