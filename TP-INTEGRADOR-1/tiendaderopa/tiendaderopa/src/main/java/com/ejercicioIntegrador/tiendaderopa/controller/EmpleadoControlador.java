package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoEmpleado;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Empleado;
import com.ejercicioIntegrador.tiendaderopa.service.AdminPageSupport;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioEmpleado;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/admin/empleados")
public class EmpleadoControlador {

    private final ServicioEmpleado servicio;

    public EmpleadoControlador(ServicioEmpleado servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) {
        return mostrar(model, null);
    }

    @GetMapping("/activos")
    public String listarActivos(Model model) {
        AdminPageSupport.cargar(model, "Empleados activos", "/admin/empleados", Empleado.class,
                servicio.listarEmpleadoActivo(), campos(), null);
        model.addAttribute("asociacionUsuarios", true);
        return "admin/registros";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            return mostrar(model, servicio.buscarEmpleado(id));
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/empleados";
        }
    }

    @PostMapping
    public String crear(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String documento,
            @RequestParam TipoEmpleado tipoEmpleado,
            @RequestParam(required = false) String idEmpresa,
            RedirectAttributes redirect
    ) {
        try {
            servicio.crearEmpleado(nombre, apellido, fechaNacimiento, tipoDocumento, documento, tipoEmpleado, idEmpresa);
            redirect.addFlashAttribute("mensaje", "Empleado creado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    @PostMapping("/{id}")
    public String modificar(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaNacimiento,
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String documento,
            @RequestParam TipoEmpleado tipoEmpleado,
            @RequestParam(required = false) String idEmpresa,
            RedirectAttributes redirect
    ) {
        try {
            servicio.modificarEmpleado(id, nombre, apellido, fechaNacimiento, tipoDocumento,
                    documento, tipoEmpleado, idEmpresa);
            redirect.addFlashAttribute("mensaje", "Empleado actualizado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            servicio.eliminarEmpleado(id);
            redirect.addFlashAttribute("mensaje", "Empleado eliminado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    @PostMapping("/{idEmpleado}/usuario")
    public String asociarUsuario(@PathVariable String idEmpleado, @RequestParam String usuarioId,
            RedirectAttributes redirect) {
        try {
            servicio.asociarEmpleadoUsuario(idEmpleado, usuarioId);
            redirect.addFlashAttribute("mensaje", "Usuario asociado correctamente.");
        } catch (MiException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    private String mostrar(Model model, Empleado seleccionado) {
        AdminPageSupport.cargar(model, "Empleados", "/admin/empleados", Empleado.class,
                servicio.listarEmpleado(), campos(), seleccionado);
        model.addAttribute("asociacionUsuarios", true);
        return "admin/registros";
    }

    private java.util.List<java.util.Map<String, Object>> campos() {
        return java.util.List.of(
                        AdminPageSupport.campo("nombre", "text", true),
                        AdminPageSupport.campo("apellido", "text", true),
                        AdminPageSupport.campo("fechaNacimiento", "date", true),
                        AdminPageSupport.campo("tipoDocumento", "select", true,
                                java.util.Arrays.stream(TipoDocumento.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campo("documento", "text", true),
                        AdminPageSupport.campo("tipoEmpleado", "select", true,
                                java.util.Arrays.stream(TipoEmpleado.values()).map(Enum::name).toArray(String[]::new)),
                        AdminPageSupport.campo("idEmpresa", "text", false));
    }
}