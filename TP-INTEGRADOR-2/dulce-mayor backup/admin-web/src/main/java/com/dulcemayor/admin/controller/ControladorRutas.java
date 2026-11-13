package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.RutaDTO;
import com.dulcemayor.admin.dto.RutaFormularioDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioRutas;
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

import java.time.LocalDate;

/** HU-18: rutas de visita de los preventistas (listado por fecha, alta y baja). */
@Controller
@RequestMapping("/rutas")
public class ControladorRutas {

    private final ServicioRutas servicioRutas;
    private final GestorSesion gestorSesion;

    public ControladorRutas(ServicioRutas servicioRutas, GestorSesion gestorSesion) {
        this.servicioRutas = servicioRutas;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String fecha, HttpServletRequest request, Model model) {
        String dia = fecha == null || fecha.isBlank() ? LocalDate.now().toString() : fecha;
        model.addAttribute("rutas", servicioRutas.listarPorFecha(token(request), dia));
        model.addAttribute("fecha", dia);
        return "rutas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(HttpServletRequest request, Model model) {
        RutaFormularioDTO formulario = new RutaFormularioDTO();
        formulario.setFecha(LocalDate.now().toString());
        return formulario(formulario, null, request, model);
    }

    @PostMapping
    public String crear(@ModelAttribute("formulario") RutaFormularioDTO formulario, HttpServletRequest request,
                        Model model, RedirectAttributes redireccion) {
        try {
            RutaDTO ruta = servicioRutas.crear(token(request), formulario);
            redireccion.addFlashAttribute("exito", "Ruta asignada a " + ruta.getNombreEmpleado() + " con "
                    + ruta.getPuntos().size() + " puntos. La app la ordena desde su ubicación.");
            return "redirect:/rutas?fecha=" + ruta.getFecha();
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable String id, @RequestParam(required = false) String fecha,
                            HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            servicioRutas.darDeBaja(token(request), id);
            redireccion.addFlashAttribute("exito", "La ruta fue dada de baja.");
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rutas" + (fecha == null ? "" : "?fecha=" + fecha);
    }

    private String formulario(RutaFormularioDTO formulario, String error, HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("formulario", formulario);
        model.addAttribute("preventistas", servicioRutas.preventistas(token));
        model.addAttribute("puntosDeVenta", servicioRutas.puntosDeVenta(token));
        model.addAttribute("error", error);
        return "rutas/formulario";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
