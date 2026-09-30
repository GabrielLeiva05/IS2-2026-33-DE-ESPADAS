package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.PersonaServicio;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProveedor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/contactos/telefono")
public class ContactoTelefonicoControlador {

    private final ServicioContactoTelefonico servicio;
    private final ServicioProveedor servicioProveedor;
    private final PersonaServicio personaServicio;

    public ContactoTelefonicoControlador(ServicioContactoTelefonico servicio, ServicioProveedor servicioProveedor,
            PersonaServicio personaServicio) {
        this.servicio = servicio;
        this.servicioProveedor = servicioProveedor;
        this.personaServicio = personaServicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null, servicio.listarContactoTelefonico());
    }

    @GetMapping("/activos")
    public String listarActivos(Model model) {
        return mostrar(model, null, servicio.listarContactoTelefonicoActivo());
    }

    @PostMapping
    public String crear(
            @RequestParam String telefono,
            @RequestParam TipoTelefono tipoTelefono,
            @RequestParam TipoContacto tipoContacto,
            @RequestParam(required = false) String observacion,
            @RequestParam(required = false) String personaId,
            @RequestParam(required = false) String proveedorId,
            RedirectAttributes redirect
    ) {
        try {
            servicio.crearContactoTelefonico(telefono, tipoTelefono, tipoContacto, observacion, personaId, proveedorId);
            redirect.addFlashAttribute("mensaje", "Contacto telefónico creado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contactos/telefono";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            ContactoTelefonico contacto = servicio.listarContactoTelefonico().stream()
                    .filter(actual -> actual.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new MiException("No existe un contacto telefónico con id: " + id));
            return mostrar(model, contacto, servicio.listarContactoTelefonico());
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/contactos/telefono";
        }
    }

    @PostMapping("/{id}")
    public String modificar(
            @PathVariable String id,
            @RequestParam String telefono,
            @RequestParam TipoTelefono tipoTelefono,
            @RequestParam TipoContacto tipoContacto,
            @RequestParam(required = false) String observacion,
            RedirectAttributes redirect
    ) {
        try {
            servicio.modificarContactoTelefonico(id, telefono, tipoTelefono, tipoContacto, observacion);
            redirect.addFlashAttribute("mensaje", "Contacto telefónico actualizado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contactos/telefono";
    }

    private String mostrar(Model model, ContactoTelefonico seleccionado,
            List<ContactoTelefonico> contactos) {
        AdminPageSupport.cargar(model, "Contactos telefónicos", "/admin/contactos/telefono",
                ContactoTelefonico.class, contactos, java.util.List.of(
                        AdminPageSupport.campo("telefono", "tel", true),
                        AdminPageSupport.campo("tipoTelefono", "select", true,
                                java.util.Arrays.stream(TipoTelefono.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campo("tipoContacto", "select", true,
                                java.util.Arrays.stream(TipoContacto.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campo("observacion", "text", false),
                        AdminPageSupport.campoRelacion("personaId", false, AdminPageSupport.mapaOpciones(
                                personaServicio.listarPersona(), Persona::getId,
                                persona -> persona.getNombre() + " " + persona.getApellido())),
                        AdminPageSupport.campoRelacion("proveedorId", false, AdminPageSupport.mapaOpciones(
                                servicioProveedor.listarProveedorActivo(), Proveedor::getId, Proveedor::getRazonSocial))),
                seleccionado);
                model.addAttribute("permitirVerDetalle", true);
                model.addAttribute("detallePath", "/admin/contactos");
        model.addAttribute("permitirEliminar", false);
        return "admin/registros";
    }
}
