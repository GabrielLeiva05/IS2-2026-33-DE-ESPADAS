package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.EmpleadoDTO;
import com.dulcemayor.admin.dto.EmpleadoFormularioDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioEmpleados;
import com.dulcemayor.admin.service.ServicioUbicacion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-07: alta, modificación, listado y baja de empleados. Solo el jefe administrativo. */
@Controller
@RequestMapping("/empleados")
public class ControladorEmpleados {

    private final ServicioEmpleados servicioEmpleados;
    private final ServicioUbicacion servicioUbicacion;
    private final GestorSesion gestorSesion;

    public ControladorEmpleados(ServicioEmpleados servicioEmpleados, ServicioUbicacion servicioUbicacion,
                                GestorSesion gestorSesion) {
        this.servicioEmpleados = servicioEmpleados;
        this.servicioUbicacion = servicioUbicacion;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String rol, HttpServletRequest request, Model model) {
        model.addAttribute("empleados", servicioEmpleados.listar(token(request), rol));
        model.addAttribute("rolFiltro", rol == null ? "" : rol);
        return "empleados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest request, Model model) {
        return formulario(new EmpleadoFormularioDTO(), null, request, model);
    }

    @PostMapping
    public String crear(@ModelAttribute("formulario") EmpleadoFormularioDTO formulario, HttpServletRequest request,
                        Model model, RedirectAttributes redireccion) {
        try {
            EmpleadoDTO creado = servicioEmpleados.crear(token(request), formulario);
            redireccion.addFlashAttribute("exito", creado.getNombreCompleto() + " ya puede ingresar con su correo "
                    + creado.getCorreo() + " y la contraseña temporal. Deberá cambiarla en su primer ingreso.");
            return "redirect:/empleados";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, HttpServletRequest request, Model model) {
        EmpleadoDTO empleado = servicioEmpleados.consultar(token(request), id);
        return formulario(EmpleadoFormularioDTO.desde(empleado), null, request, model);
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @ModelAttribute("formulario") EmpleadoFormularioDTO formulario,
                            HttpServletRequest request, Model model, RedirectAttributes redireccion) {
        formulario.setId(id);
        try {
            EmpleadoDTO modificado = servicioEmpleados.modificar(token(request), id, formulario);
            redireccion.addFlashAttribute("exito", "Se guardaron los cambios de " + modificado.getNombreCompleto() + ".");
            return "redirect:/empleados";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable String id, HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            servicioEmpleados.darDeBaja(token(request), id);
            redireccion.addFlashAttribute("exito", "El empleado fue dado de baja y su cuenta quedó deshabilitada.");
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleados";
    }

    private String formulario(EmpleadoFormularioDTO formulario, String error, HttpServletRequest request, Model model) {
        model.addAttribute("formulario", formulario);
        model.addAttribute("opciones", servicioUbicacion.opcionesPara(token(request), formulario));
        model.addAttribute("roles", servicioEmpleados.rolesAsignables());
        model.addAttribute("error", error);
        return "empleados/formulario";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
