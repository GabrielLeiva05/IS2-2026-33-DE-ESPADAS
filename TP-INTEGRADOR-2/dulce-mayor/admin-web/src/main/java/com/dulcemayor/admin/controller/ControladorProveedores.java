package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.ProveedorDTO;
import com.dulcemayor.admin.dto.ProveedorFormularioDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioFacturas;
import com.dulcemayor.admin.service.ServicioUbicacion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** ABM de proveedores (Ola 3) para ADMINISTRATIVO y JEFE. */
@Controller
@RequestMapping("/proveedores")
public class ControladorProveedores {

    private final ServicioFacturas servicioFacturas;
    private final ServicioUbicacion servicioUbicacion;
    private final GestorSesion gestorSesion;

    public ControladorProveedores(ServicioFacturas servicioFacturas, ServicioUbicacion servicioUbicacion,
                                  GestorSesion gestorSesion) {
        this.servicioFacturas = servicioFacturas;
        this.servicioUbicacion = servicioUbicacion;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(HttpServletRequest request, Model model) {
        model.addAttribute("proveedores", servicioFacturas.proveedores(token(request)));
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest request, Model model) {
        return formulario(new ProveedorFormularioDTO(), null, request, model);
    }

    @PostMapping
    public String crear(@ModelAttribute("formulario") ProveedorFormularioDTO formulario, HttpServletRequest request,
                        Model model, RedirectAttributes redireccion) {
        try {
            ProveedorDTO creado = servicioFacturas.crearProveedor(token(request), formulario);
            redireccion.addFlashAttribute("exito", "Se dio de alta " + creado.getRazonSocial() + ".");
            return "redirect:/proveedores";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, HttpServletRequest request, Model model) {
        return formulario(ProveedorFormularioDTO.desde(servicioFacturas.proveedor(token(request), id)), null, request, model);
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @ModelAttribute("formulario") ProveedorFormularioDTO formulario,
                            HttpServletRequest request, Model model, RedirectAttributes redireccion) {
        formulario.setId(id);
        try {
            ProveedorDTO modificado = servicioFacturas.modificarProveedor(token(request), id, formulario);
            redireccion.addFlashAttribute("exito", "Se guardaron los cambios de " + modificado.getRazonSocial() + ".");
            return "redirect:/proveedores";
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
            servicioFacturas.darDeBajaProveedor(token(request), id);
            redireccion.addFlashAttribute("exito", "El proveedor fue dado de baja. Sus facturas se conservan.");
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }

    private String formulario(ProveedorFormularioDTO formulario, String error, HttpServletRequest request, Model model) {
        model.addAttribute("formulario", formulario);
        model.addAttribute("opciones", servicioUbicacion.opcionesPara(token(request), formulario));
        model.addAttribute("error", error);
        return "proveedores/formulario";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
