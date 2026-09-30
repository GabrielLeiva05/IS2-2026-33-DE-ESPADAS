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
 * ABM de empleados embebido en el dashboard (fragmento fragments/abmEmpleado.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/empleados) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/empleado")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class EmpleadoAbmControlador {

    private final ServicioEmpleado servicioEmpleado;
    private final ServicioEmpresa servicioEmpresa;

    public EmpleadoAbmControlador(ServicioEmpleado servicioEmpleado, ServicioEmpresa servicioEmpresa) {
        this.servicioEmpleado = servicioEmpleado;
        this.servicioEmpresa = servicioEmpresa;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String apellido,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
                           @RequestParam TipoDocumento tipoDocumento,
                           @RequestParam String documento,
                           @RequestParam TipoEmpleado tipoEmpleado,
                           @RequestParam(required = false) String idEmpresa,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioEmpleado.crearEmpleado(nombre, apellido, fechaNacimiento, tipoDocumento, documento, tipoEmpleado, idEmpresa);
            redirectAttributes.addFlashAttribute("exito", "Empleado cargado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("empleado", servicioEmpleado.buscarEmpleado(id));
            modelo.addAttribute("empleados", servicioEmpleado.listarEmpleado());
            modelo.addAttribute("empresas", servicioEmpresa.listarEmpresa());
            return "panel.html";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR / MODIFICAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                           @RequestParam String nombre,
                           @RequestParam String apellido,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
                           @RequestParam TipoDocumento tipoDocumento,
                           @RequestParam String documento,
                           @RequestParam TipoEmpleado tipoEmpleado,
                           @RequestParam(required = false) String idEmpresa,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioEmpleado.modificarEmpleado(id, nombre, apellido, fechaNacimiento, tipoDocumento, documento, tipoEmpleado, idEmpresa);
            redirectAttributes.addFlashAttribute("exito", "Empleado modificado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioEmpleado.eliminarEmpleado(id);
            redirectAttributes.addFlashAttribute("exito", "Empleado eliminado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
