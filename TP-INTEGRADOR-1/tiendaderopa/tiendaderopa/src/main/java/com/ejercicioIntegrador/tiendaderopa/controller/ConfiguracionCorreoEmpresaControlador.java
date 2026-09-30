package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ConfiguracionCorreoEmpresa;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioConfiguracionCorreoEmpresa;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/configuraciones-correo")
public class ConfiguracionCorreoEmpresaControlador {

    private final ServicioConfiguracionCorreoEmpresa servicio;

    public ConfiguracionCorreoEmpresaControlador(ServicioConfiguracionCorreoEmpresa servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/activas")
    public String listarActivas(Model model) {
        AdminPageSupport.cargar(model, "Configuraciones de correo", "/admin/configuraciones-correo",
                ConfiguracionCorreoEmpresa.class, servicio.listarConfiguracionCorreoAutomaticoActiva(), campos(), null);
        return "admin/registros";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            ConfiguracionCorreoEmpresa configuracion = servicio.buscarConfiguracionCorreoAutomatico(id);
            return mostrar(model, configuracion);
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/configuraciones-correo";
        }
    }

    @PostMapping
    public String crear(
            @RequestParam String correo,
            @RequestParam String clave,
            @RequestParam String puerto,
            @RequestParam String smtp,
            @RequestParam(defaultValue = "false") boolean tls,
            @RequestParam String idEmpresa,
            RedirectAttributes redirect
    ) {
        try {
            servicio.crearConfiguracionCorreoAutomatico(correo, clave, puerto, smtp, tls, idEmpresa);
            redirect.addFlashAttribute("mensaje", "Configuración creada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/configuraciones-correo";
    }

    @PostMapping("/{id}")
    public String modificar(
            @PathVariable String id,
            @RequestParam String correo,
            @RequestParam String clave,
            @RequestParam String puerto,
            @RequestParam String smtp,
            @RequestParam(defaultValue = "false") boolean tls,
            @RequestParam String idEmpresa,
            RedirectAttributes redirect
    ) {
        try {
            servicio.modificarConfiguracionCorreoAutomatico(id, correo, clave, puerto, smtp, tls, idEmpresa);
            redirect.addFlashAttribute("mensaje", "Configuración actualizada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/configuraciones-correo";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarConfiguracionCorreoAutomatico(id);
            redirect.addFlashAttribute("mensaje", "Configuración eliminada correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/configuraciones-correo";
    }

    @PostMapping("/{id}/enviar")
    public String enviarCorreo(
            @PathVariable String id,
            @RequestParam String destinatario,
            @RequestParam String asunto,
            @RequestParam String cuerpoHtml,
            RedirectAttributes redirect
    ) {
        try {
            servicio.enviarCorreo(id, destinatario, asunto, cuerpoHtml);
            redirect.addFlashAttribute("mensaje", "Correo enviado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/configuraciones-correo";
    }

    private String mostrar(Model model, ConfiguracionCorreoEmpresa seleccionada) {
        AdminPageSupport.cargar(model, "Configuraciones de correo", "/admin/configuraciones-correo",
                ConfiguracionCorreoEmpresa.class, servicio.listarConfiguracionCorreoAutomatico(), campos(), seleccionada);
        if (seleccionada != null) {
            model.addAttribute("accionSecundaria", "/admin/configuraciones-correo/" + seleccionada.getId() + "/enviar");
            model.addAttribute("camposSecundarios", List.of(
                    AdminPageSupport.campo("destinatario", "email", true),
                    AdminPageSupport.campo("asunto", "text", true),
                    AdminPageSupport.campo("cuerpoHtml", "textarea", true)));
        }
        return "admin/registros";
    }

    private List<java.util.Map<String, Object>> campos() {
        return List.of(
                AdminPageSupport.campo("correo", "email", true),
                AdminPageSupport.campo("clave", "password", true),
                AdminPageSupport.campo("puerto", "number", true),
                AdminPageSupport.campo("smtp", "text", true),
                AdminPageSupport.campo("tls", "checkbox", false),
                AdminPageSupport.campo("idEmpresa", "text", true));
    }
}