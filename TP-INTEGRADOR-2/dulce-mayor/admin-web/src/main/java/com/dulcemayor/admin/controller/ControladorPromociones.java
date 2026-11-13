package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.PromocionDTO;
import com.dulcemayor.admin.dto.PromocionFormularioDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioPromociones;
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

/** Promociones y códigos de descuento (HU-22) para ADMINISTRATIVO y JEFE. */
@Controller
@RequestMapping("/promociones")
public class ControladorPromociones {

    private final ServicioPromociones servicioPromociones;
    private final GestorSesion gestorSesion;

    public ControladorPromociones(ServicioPromociones servicioPromociones, GestorSesion gestorSesion) {
        this.servicioPromociones = servicioPromociones;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(HttpServletRequest request, Model model) {
        model.addAttribute("promociones", servicioPromociones.listar(token(request)));
        return "promociones/lista";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) String tipo, HttpServletRequest request, Model model) {
        return formulario(PromocionFormularioDTO.nuevo(tipo), null, request, model);
    }

    @PostMapping
    public String crear(@ModelAttribute("formulario") PromocionFormularioDTO formulario, HttpServletRequest request,
                        Model model, RedirectAttributes redireccion) {
        try {
            PromocionDTO creada = servicioPromociones.crear(token(request), formulario);
            redireccion.addFlashAttribute("exito", "Se creó la promoción «" + creada.getNombre() + "».");
            return "redirect:/promociones";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, HttpServletRequest request, Model model) {
        return formulario(PromocionFormularioDTO.desde(servicioPromociones.obtener(token(request), id)), null, request, model);
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @ModelAttribute("formulario") PromocionFormularioDTO formulario,
                            HttpServletRequest request, Model model, RedirectAttributes redireccion) {
        formulario.setId(id);
        try {
            PromocionDTO modificada = servicioPromociones.modificar(token(request), id, formulario);
            redireccion.addFlashAttribute("exito", "Se guardaron los cambios de «" + modificada.getNombre()
                    + "». Los pedidos ya cargados conservan el descuento que se les aplicó.");
            return "redirect:/promociones";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    @PostMapping("/{id}/activa")
    public String cambiarActiva(@PathVariable String id, @RequestParam boolean activa, HttpServletRequest request,
                                RedirectAttributes redireccion) {
        try {
            PromocionDTO p = servicioPromociones.cambiarActiva(token(request), id, activa);
            redireccion.addFlashAttribute("exito", "«" + p.getNombre() + "» quedó " + (activa ? "activada." : "desactivada."));
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/promociones";
    }

    @PostMapping("/{id}/baja")
    public String eliminar(@PathVariable String id, HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            servicioPromociones.eliminar(token(request), id);
            redireccion.addFlashAttribute("exito", "La promoción se eliminó. Los pedidos que la usaron conservan su descuento.");
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/promociones";
    }

    private String formulario(PromocionFormularioDTO formulario, String error, HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("formulario", formulario);
        model.addAttribute("productos", servicioPromociones.productos(token));
        model.addAttribute("subcategorias", servicioPromociones.subcategorias(token));
        model.addAttribute("error", error);
        return "promociones/formulario";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
