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
 * ABM de vigencias de precio embebido en el dashboard (fragmento fragments/abmVigenciaPrecio.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/precios) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/vigencia-precio")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class VigenciaPrecioAbmControlador {

    private final ServicioVigenciaPrecio servicioVigenciaPrecio;
    private final ServicioProducto servicioProducto;

    public VigenciaPrecioAbmControlador(ServicioVigenciaPrecio servicioVigenciaPrecio, ServicioProducto servicioProducto) {
        this.servicioVigenciaPrecio = servicioVigenciaPrecio;
        this.servicioProducto = servicioProducto;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String idProducto,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
                           @RequestParam double precio,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioVigenciaPrecio.crearVigenciaPrecio(fechaDesde, fechaHasta, precio, idProducto);
            redirectAttributes.addFlashAttribute("exito", "Vigencia de precio cargada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("vigencia", servicioVigenciaPrecio.buscarVigenciaPrecio(id));
            modelo.addAttribute("vigencias", servicioVigenciaPrecio.listarVigenciaPrecio());
            modelo.addAttribute("productos", servicioProducto.listarProducto());
            return "panel.html";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                           @RequestParam String idProducto,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
                           @RequestParam double precio,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioVigenciaPrecio.modificarVigenciaPrecio(id, fechaDesde, fechaHasta, precio, idProducto);
            redirectAttributes.addFlashAttribute("exito", "Vigencia de precio modificada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioVigenciaPrecio.eliminarVigenciaPrecio(id);
            redirectAttributes.addFlashAttribute("exito", "Vigencia de precio eliminada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
