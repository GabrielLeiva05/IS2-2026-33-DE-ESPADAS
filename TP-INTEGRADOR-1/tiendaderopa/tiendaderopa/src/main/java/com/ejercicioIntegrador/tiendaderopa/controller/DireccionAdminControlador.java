package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.DireccionServicio;
import com.ejercicioIntegrador.tiendaderopa.service.LocalidadServicio;
import com.ejercicioIntegrador.tiendaderopa.service.PersonaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/direcciones")
public class DireccionAdminControlador {

    private final DireccionServicio servicio;
    private final LocalidadServicio localidadServicio;
    private final PersonaServicio personaServicio;

    public DireccionAdminControlador(DireccionServicio servicio, LocalidadServicio localidadServicio,
            PersonaServicio personaServicio) {
        this.servicio = servicio;
        this.localidadServicio = localidadServicio;
        this.personaServicio = personaServicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            return mostrar(model, servicio.buscarPorId(id));
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/direcciones";
        }
    }

    @PostMapping
    public String crear(@RequestParam String calle, @RequestParam String numeracion,
            @RequestParam String codigoPostal, @RequestParam(required = false) String barrio,
            @RequestParam(required = false) String manzanaPiso, @RequestParam(required = false) String casaDepartamento,
            @RequestParam(required = false) String referencia, @RequestParam String localidadId,
            @RequestParam String personaId, RedirectAttributes redirect) {
        try {
            Localidad localidad = localidadServicio.buscarPorId(localidadId);
            Persona persona = personaServicio.buscarPersona(personaId);
            servicio.crearDireccionAdmin(persona, localidad, codigoPostal, barrio, calle, numeracion, manzanaPiso,
                    casaDepartamento, referencia);
            redirect.addFlashAttribute("mensaje", "Dirección creada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/direcciones";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String calle, @RequestParam String numeracion,
            @RequestParam String codigoPostal, @RequestParam(required = false) String barrio,
            @RequestParam(required = false) String manzanaPiso, @RequestParam(required = false) String casaDepartamento,
            @RequestParam(required = false) String referencia, @RequestParam String localidadId,
            @RequestParam String personaId, RedirectAttributes redirect) {
        try {
            Localidad localidad = localidadServicio.buscarPorId(localidadId);
            Persona persona = personaServicio.buscarPersona(personaId);
            servicio.modificarDireccionAdmin(id, persona, localidad, codigoPostal, barrio, calle, numeracion,
                    manzanaPiso, casaDepartamento, referencia);
            redirect.addFlashAttribute("mensaje", "Dirección actualizada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/direcciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Dirección eliminada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/direcciones";
    }

    private String mostrar(Model model, Direccion seleccionada) {
        AdminPageSupport.cargar(model, "Direcciones", "/admin/direcciones", Direccion.class,
                servicio.listarTodas(), java.util.List.of(
                        AdminPageSupport.campo("calle", "text", true),
                        AdminPageSupport.campo("numeracion", "text", true),
                        AdminPageSupport.campo("codigoPostal", "text", true),
                        AdminPageSupport.campo("barrio", "text", false),
                        AdminPageSupport.campo("manzanaPiso", "text", false),
                        AdminPageSupport.campo("casaDepartamento", "text", false),
                        AdminPageSupport.campo("referencia", "text", false),
                        AdminPageSupport.campoRelacion("localidadId", true, AdminPageSupport.mapaOpciones(
                                localidadServicio.listarTodas(), Localidad::getId, Localidad::getNombre)),
                        AdminPageSupport.campoRelacion("personaId", true, AdminPageSupport.mapaOpciones(
                                personaServicio.listarPersona(), Persona::getId,
                                persona -> persona.getNombre() + " " + persona.getApellido()))),
                seleccionada);
        return "admin/registros";
    }
}
