package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioPromocionesAdmin;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/** Alta, consulta y baja de códigos promocionales (un código por pedido, sin acumulación). */
@Controller
@RequestMapping("/promociones")
public class ControladorPromociones {

    private static final List<String> TIPOS = List.of("PORCENTAJE", "MONTO_FIJO");

    private final ServicioPromocionesAdmin servicioPromociones;
    private final GestorSesion gestorSesion;

    public ControladorPromociones(ServicioPromocionesAdmin servicioPromociones, GestorSesion gestorSesion) {
        this.servicioPromociones = servicioPromociones;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(HttpServletRequest request, Model model) {
        model.addAttribute("promociones", servicioPromociones.listar(token(request)));
        model.addAttribute("tipos", TIPOS);
        return "promociones/lista";
    }

    @PostMapping
    public String alta(@RequestParam String codigo, @RequestParam String descripcion, @RequestParam String tipo,
                       @RequestParam BigDecimal valor,
                       @RequestParam(required = false) String fechaDesde,
                       @RequestParam(required = false) String fechaHasta,
                       @RequestParam(defaultValue = "false") boolean usoUnico,
                       @RequestParam(required = false) BigDecimal minimoCompra,
                       HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            var creada = servicioPromociones.alta(token(request), codigo, descripcion, tipo, valor,
                    fechaDesde, fechaHasta, true, usoUnico, minimoCompra);
            redireccion.addFlashAttribute("exito", "Se creó la promoción " + creada.getCodigo() + ".");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/promociones";
    }

    @PostMapping("/{id}/baja")
    public String baja(@PathVariable String id, HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            servicioPromociones.baja(token(request), id);
            redireccion.addFlashAttribute("exito", "La promoción quedó desactivada.");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/promociones";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
