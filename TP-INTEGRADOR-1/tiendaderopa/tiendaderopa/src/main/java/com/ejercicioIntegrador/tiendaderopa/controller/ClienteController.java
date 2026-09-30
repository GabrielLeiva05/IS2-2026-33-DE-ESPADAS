package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.model.Cliente;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "false") boolean incluirInactivos, Model model) {
        List<Cliente> clientes = incluirInactivos ? service.listarCliente() : service.listarClienteActivo();
        model.addAttribute("clientes", clientes);
        model.addAttribute("incluirInactivos", incluirInactivos);
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "admin/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model) {
        model.addAttribute("cliente", service.buscarCliente(id));
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "admin/cliente-form";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "admin/cliente-form";
    }

    @PostMapping
    public String crear(@RequestParam String nombre, @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento, @RequestParam String documento,
            @RequestParam(required = false) String direccionEstadia, RedirectAttributes redirect) {
        try {
            service.crearCliente(nombre, apellido, fechaNacimiento, tipoDocumento, documento, direccionEstadia);
            redirect.addFlashAttribute("mensaje", "Cliente creado correctamente.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/clientes/nuevo";
        }
        return "redirect:/admin/clientes";
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @RequestParam String nombre, @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento, @RequestParam String documento,
            @RequestParam(required = false) String direccionEstadia, RedirectAttributes redirect) {
        try {
            service.modificarCliente(id, nombre, apellido, fechaNacimiento, tipoDocumento, documento, direccionEstadia);
            redirect.addFlashAttribute("mensaje", "Cliente actualizado correctamente.");
            return "redirect:/admin/clientes";
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/clientes/" + id + "/editar";
        }
    }

    @PostMapping("/{clienteId}/asociar-usuario")
    public String asociarUsuario(@PathVariable String clienteId, @RequestParam String usuarioId,
            RedirectAttributes redirect) {
        try {
            service.asociarClienteUsuario(clienteId, usuarioId);
            redirect.addFlashAttribute("mensaje", "Usuario asociado correctamente.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/clientes";
    }
}