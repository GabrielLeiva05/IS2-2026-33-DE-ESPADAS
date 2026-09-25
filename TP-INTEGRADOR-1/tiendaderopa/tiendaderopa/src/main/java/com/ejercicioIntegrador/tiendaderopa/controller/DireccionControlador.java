package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.service.DireccionServicio;
import com.ejercicioIntegrador.tiendaderopa.service.LocalidadServicio;
import com.ejercicioIntegrador.tiendaderopa.service.PersonaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/direccion")
public class DireccionControlador {

    private final DireccionServicio direccionServicio;
    private final LocalidadServicio localidadServicio;
    private final PersonaServicio personaServicio;

    public DireccionControlador(DireccionServicio direccionServicio,
                                LocalidadServicio localidadServicio,
                                PersonaServicio personaServicio) {
        this.direccionServicio = direccionServicio;
        this.localidadServicio = localidadServicio;
        this.personaServicio = personaServicio;
    }

    // 1. GUARDAR / REGISTRAR (POST)
    @PostMapping("/registro")
    public String registro(@RequestParam String calle,
                           @RequestParam String numeracion,
                           @RequestParam(required = false) String barrio,
                           @RequestParam(required = false) String manzanaPiso,
                           @RequestParam(required = false) String casaDepartamento,
                           @RequestParam(required = false) String referencia,
                           @RequestParam String idLocalidad,
                           @RequestParam String idPersona,
                           RedirectAttributes redirectAttributes) {
        try {
            Localidad localidad = localidadServicio.buscarPorId(idLocalidad);
            Persona persona = personaServicio.buscarPersona(idPersona);

            direccionServicio.crearDireccion(calle, numeracion, barrio, manzanaPiso, casaDepartamento, referencia, localidad, persona);
            redirectAttributes.addFlashAttribute("exito", "Dirección registrada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 2. VISTA MODIFICAR (Carga el panel con los datos de la dirección a editar)
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable String id, ModelMap modelo) {
        try {
            Direccion direccion = direccionServicio.buscarPorId(id);
            modelo.addAttribute("direccion", direccion);
            modelo.addAttribute("direcciones", direccionServicio.listarTodas());
            modelo.addAttribute("localidades", localidadServicio.listarTodas());
            modelo.addAttribute("personas", personaServicio.listarTodas());

            // Reseteamos las demás entidades de edición para no solapar formularios en panel.html
            modelo.addAttribute("localidad", null);
            modelo.addAttribute("departamento", null);
            modelo.addAttribute("provincia", null);
            modelo.addAttribute("pais", null);

            return "panel.html";
        } catch (MiException ex) {
            modelo.addAttribute("error", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // 3. ACTUALIZAR (POST)
    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable String id,
                            @RequestParam String calle,
                            @RequestParam String numeracion,
                            @RequestParam(required = false) String barrio,
                            @RequestParam(required = false) String manzanaPiso,
                            @RequestParam(required = false) String casaDepartamento,
                            @RequestParam(required = false) String referencia,
                            @RequestParam String idLocalidad,
                            @RequestParam String idPersona,
                            RedirectAttributes redirectAttributes) {
        try {
            Localidad localidad = localidadServicio.buscarPorId(idLocalidad);
            Persona persona = personaServicio.buscarPersona(idPersona);

            direccionServicio.modificarDireccion(id, calle, numeracion, barrio, manzanaPiso, casaDepartamento, referencia, localidad, persona);
            redirectAttributes.addFlashAttribute("exito", "Dirección modificada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // 4. ELIMINAR (GET)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            direccionServicio.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Dirección eliminada correctamente.");
        } catch (MiException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}