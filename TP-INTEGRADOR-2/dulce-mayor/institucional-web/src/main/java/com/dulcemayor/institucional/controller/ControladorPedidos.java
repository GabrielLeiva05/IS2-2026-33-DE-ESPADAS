package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.dto.PedidoDTO;
import com.dulcemayor.institucional.seguridad.GestorSesion;
import com.dulcemayor.institucional.service.ServicioPedidos;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * "Mis pedidos": el cliente ve los pedidos que le cargó el preventista y los confirma o anula.
 * Confirmar y anular son POST (nunca GET), y las reglas las aplica el backend.
 */
@Controller
@RequestMapping("/mis-pedidos")
public class ControladorPedidos {

    private final ServicioPedidos servicioPedidos;
    private final GestorSesion gestorSesion;

    public ControladorPedidos(ServicioPedidos servicioPedidos, GestorSesion gestorSesion) {
        this.servicioPedidos = servicioPedidos;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String lista(HttpServletRequest request, Model model) {
        List<PedidoDTO> pedidos = servicioPedidos.misPedidos(token(request));
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("porConfirmar", pedidos.stream().filter(PedidoDTO::isPuedeConfirmarse).count());
        return "pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, HttpServletRequest request, Model model) {
        model.addAttribute("pedido", servicioPedidos.miPedido(token(request), id));
        return "pedidos/detalle";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable String id, HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            PedidoDTO pedido = servicioPedidos.confirmar(token(request), id);
            redireccion.addFlashAttribute("exito", "Confirmaste el pedido N.º " + pedido.getNumeroPedido()
                    + ". Vas a recibir por correo el detalle, el total y las instrucciones de pago.");
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mis-pedidos/{id}";
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable String id, HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            PedidoDTO pedido = servicioPedidos.anular(token(request), id);
            redireccion.addFlashAttribute("exito", "Anulaste el pedido N.º " + pedido.getNumeroPedido() + ".");
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mis-pedidos/{id}";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
