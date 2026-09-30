package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoCorreoElectronico;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioContactoCorreoElectronico;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioProveedor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/contactos/correo")
public class ContactoCorreoElectronicoControlador {

    private final ServicioContactoCorreoElectronico servicio;

    public ContactoCorreoElectronicoControlador(ServicioContactoCorreoElectronico servicio, ServicioProveedor servicioProveedor) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null, servicio.listarContactoCorreoElectronico());
    }

    @GetMapping("/activos")
    public String listarActivos(Model model) {
        return mostrar(model, null, servicio.listarContactoCorreoElectronicoActivo());
    }

    @PostMapping
    public String crear(
            @RequestParam String email,
            @RequestParam TipoContacto tipoContacto,
            @RequestParam(required = false) String observacion,
            @RequestParam(required = false) String personaId,
            @RequestParam(required = false) String proveedorId,
            RedirectAttributes redirect
    ) {
        try {
            servicio.crearContactoCorreoElectronico(email, tipoContacto, observacion, personaId, proveedorId);
            redirect.addFlashAttribute("mensaje", "Contacto de correo creado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contactos/correo";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            ContactoCorreoElectronico contacto = servicio.listarContactoCorreoElectronico().stream()
                    .filter(actual -> actual.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new MiException("No existe un contacto de correo electrónico con id: " + id));
            return mostrar(model, contacto, servicio.listarContactoCorreoElectronico());
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/contactos/correo";
        }
    }

    @PostMapping("/{id}")
    public String modificar(
            @PathVariable String id,
            @RequestParam String email,
            @RequestParam TipoContacto tipoContacto,
            @RequestParam(required = false) String observacion,
            RedirectAttributes redirect
    ) {
        try {
            servicio.modificarContactoCorreoElectronico(id, email, tipoContacto, observacion);
            redirect.addFlashAttribute("mensaje", "Contacto de correo actualizado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contactos/correo";
    }

    private String mostrar(Model model, ContactoCorreoElectronico seleccionado,
            List<ContactoCorreoElectronico> contactos) {
        AdminPageSupport.cargar(model, "Contactos de correo", "/admin/contactos/correo",
                ContactoCorreoElectronico.class, contactos, java.util.List.of(
                        AdminPageSupport.campo("email", "email", true),
                        AdminPageSupport.campo("tipoContacto", "select", true,
                                java.util.Arrays.stream(TipoContacto.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campo("observacion", "text", false),
                        AdminPageSupport.campo("personaId", "text", false),
                        AdminPageSupport.campo("proveedorId", "text", false)), seleccionado);
                model.addAttribute("permitirVerDetalle", true);
                model.addAttribute("detallePath", "/admin/contactos");
        model.addAttribute("permitirEliminar", false);
        return "admin/registros";
    }
}
