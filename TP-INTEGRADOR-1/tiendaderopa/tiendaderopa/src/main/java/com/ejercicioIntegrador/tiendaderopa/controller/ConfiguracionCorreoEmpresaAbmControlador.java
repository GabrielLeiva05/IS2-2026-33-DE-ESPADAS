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
 * ABM de configuraciones de correo embebido en el dashboard (fragmento fragments/abmConfiguracionCorreo.html).
 * Mismo patrón que PaisControlador / ProvinciaControlador; las rutas cuelgan de /admin/abm
 * para no chocar con las páginas completas (/admin/configuraciones-correo) ni con el sitio público.
 */
@Controller
@RequestMapping("/admin/abm/config-correo")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ConfiguracionCorreoEmpresaAbmControlador {

    private final ServicioConfiguracionCorreoEmpresa servicioConfiguracion;
    private final ServicioEmpresa servicioEmpresa;

    public ConfiguracionCorreoEmpresaAbmControlador(ServicioConfiguracionCorreoEmpresa servicioConfiguracion, ServicioEmpresa servicioEmpresa) {
        this.servicioConfiguracion = servicioConfiguracion;
        this.servicioEmpresa = servicioEmpresa;
    }

    // 1. GUARDAR / CREAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String idEmpresa,
                           @RequestParam String correo,
                           @RequestParam String clave,
                           @RequestParam String smtp,
                           @RequestParam String puerto,
                           @RequestParam(defaultValue = "false") boolean tls,
                           RedirectAttributes redirectAttributes) {
        try {
            servicioConfiguracion.crearConfiguracionCorreoAutomatico(correo, clave, puerto, smtp, tls, idEmpresa);
            redirectAttributes.addFlashAttribute("exito", "Configuración de correo cargada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo, RedirectAttributes redirectAttributes) {
        try {
            modelo.addAttribute("configuracionCorreo", servicioConfiguracion.buscarConfiguracionCorreoAutomatico(id));
            modelo.addAttribute("configuracionesCorreo", servicioConfiguracion.listarConfiguracionCorreoAutomatico());
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
                           @RequestParam String idEmpresa,
                           @RequestParam String correo,
                           @RequestParam(required = false) String clave,
                           @RequestParam String smtp,
                           @RequestParam String puerto,
                           @RequestParam(defaultValue = "false") boolean tls,
                           RedirectAttributes redirectAttributes) {
        try {
            // La clave es opcional al modificar: si viene vacía se conserva la guardada.
            String claveFinal = (clave == null || clave.isBlank())
                    ? servicioConfiguracion.buscarConfiguracionCorreoAutomatico(id).getClave()
                    : clave;
            servicioConfiguracion.modificarConfiguracionCorreoAutomatico(id, correo, claveFinal, puerto, smtp, tls, idEmpresa);
            redirectAttributes.addFlashAttribute("exito", "Configuración de correo modificada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            servicioConfiguracion.eliminarConfiguracionCorreoAutomatico(id);
            redirectAttributes.addFlashAttribute("exito", "Configuración de correo eliminada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
