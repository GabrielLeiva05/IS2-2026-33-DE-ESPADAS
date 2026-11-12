package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.AsignacionFormularioDTO;
import com.dulcemayor.admin.dto.PedidoDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioEntregas;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Asignación de pedidos confirmados a distribuidores. */
@Controller
@RequestMapping("/entregas")
public class ControladorEntregas {

    private final ServicioEntregas servicioEntregas;
    private final GestorSesion gestorSesion;

    public ControladorEntregas(ServicioEntregas servicioEntregas, GestorSesion gestorSesion) {
        this.servicioEntregas = servicioEntregas;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(HttpServletRequest request, Model model) {
        String token = token(request);
        List<PedidoDTO> pendientes = servicioEntregas.pendientesDeEntrega(token);
        model.addAttribute("sinAsignar", servicioEntregas.sinAsignar(pendientes));
        model.addAttribute("asignados", servicioEntregas.asignados(pendientes));
        model.addAttribute("distribuidores", servicioEntregas.distribuidores(token));
        model.addAttribute("formulario", new AsignacionFormularioDTO());
        return "entregas";
    }

    @PostMapping("/asignar")
    public String asignar(@ModelAttribute AsignacionFormularioDTO formulario, HttpServletRequest request,
                          RedirectAttributes redireccion) {
        try {
            int cantidad = servicioEntregas.asignar(token(request), formulario);
            redireccion.addFlashAttribute("exito", cantidad == 1 ? "Se asignó 1 pedido."
                    : "Se asignaron " + cantidad + " pedidos.");
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/entregas";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
