package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.service.MercadoPagoCheckoutService;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioOrdenCompra;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TiendaWebController {

    private final ServicioOrdenCompra ordenServicio;
    private final MercadoPagoCheckoutService checkoutService;

    public TiendaWebController(ServicioOrdenCompra ordenServicio,
            MercadoPagoCheckoutService checkoutService) {
        this.ordenServicio = ordenServicio;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/carrito")
    public String carrito(@AuthenticationPrincipal UserDetails usuario, Model model) throws Exception {
        OrdenCompra carrito = ordenServicio.obtenerCarrito(usuario.getUsername());
        model.addAttribute("carrito", carrito);
        model.addAttribute("lineas", carrito.getDetalles().stream()
                .filter(detalle -> !detalle.isEliminado())
                .toList());
        return "carrito";
    }

    @PostMapping("/carrito/items")
    public String agregar(@AuthenticationPrincipal UserDetails usuario,
            @RequestParam String productoId, @RequestParam(defaultValue = "1") int cantidad,
            RedirectAttributes redirect) {
        try {
            ordenServicio.agregarAlCarrito(productoId, cantidad, usuario.getUsername());
            redirect.addFlashAttribute("mensaje", "Producto agregado al carrito");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/items/{detalleId}/eliminar")
    public String eliminar(@AuthenticationPrincipal UserDetails usuario, @PathVariable String detalleId,
            RedirectAttributes redirect) {
        try {
            ordenServicio.eliminarDelCarrito(detalleId, usuario.getUsername());
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/pagar")
    public String pagar(@AuthenticationPrincipal UserDetails usuario, RedirectAttributes redirect) {
        try {
            OrdenCompra carrito = ordenServicio.obtenerCarrito(usuario.getUsername());
            return "redirect:" + checkoutService.iniciarCheckout(carrito.getId(), usuario.getUsername());
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/carrito";
        }
    }

    @GetMapping("/pago/resultado")
    public String resultado(
            @AuthenticationPrincipal UserDetails usuario,
            @RequestParam(name = "external_reference", required = false) String ordenId,
            @RequestParam(name = "payment_id", required = false) String paymentId,
            @RequestParam(name = "status", required = false) String estadoInformado,
            Model model) {
        model.addAttribute("orden", null);
        model.addAttribute("estadoInformado", estadoInformado);
        if (ordenId == null || ordenId.isBlank()) {
            model.addAttribute("mensaje", "No recibimos una referencia de orden desde Mercado Pago.");
            return "pago-resultado";
        }

        try {
            OrdenCompra orden = checkoutService.procesarRetorno(ordenId, paymentId, usuario.getUsername());
            model.addAttribute("orden", orden);
            if (orden.getEstadoOrdenCompra() == EstadoOrdenCompra.PAGO_REALIZADO) {
                model.addAttribute("mensaje", "El pago fue aprobado. Tu compra quedó registrada.");
            } else if ("pending".equalsIgnoreCase(orden.getMercadoPagoStatus())
                    || "in_process".equalsIgnoreCase(orden.getMercadoPagoStatus())) {
                model.addAttribute("mensaje", "Mercado Pago todavía está procesando el pago.");
            } else if (orden.getMercadoPagoStatus() == null) {
                model.addAttribute("mensaje", "Volviste de Mercado Pago. Estamos esperando la confirmación del pago.");
            } else {
                model.addAttribute("mensaje", "El pago no fue aprobado. Podés volver al carrito e intentarlo otra vez.");
            }
        } catch (Exception ex) {
            model.addAttribute("mensaje", "No pudimos verificar el pago. Revisá el estado desde tu carrito.");
        }
        return "pago-resultado";
    }
}