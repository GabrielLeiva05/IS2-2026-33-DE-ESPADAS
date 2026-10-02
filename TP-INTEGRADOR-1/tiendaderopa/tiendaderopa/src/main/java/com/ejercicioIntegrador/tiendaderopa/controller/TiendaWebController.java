package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.service.MercadoPagoCheckoutService;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioOrdenCompra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TiendaWebController {

    private static final Logger logger = LoggerFactory.getLogger(TiendaWebController.class);

    private final ServicioOrdenCompra ordenServicio;
    private final MercadoPagoCheckoutService checkoutService;

    public TiendaWebController(ServicioOrdenCompra ordenServicio,
            MercadoPagoCheckoutService checkoutService) {
        this.ordenServicio = ordenServicio;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/carrito")
    public String carrito(@AuthenticationPrincipal UserDetails usuario, Model model) throws Exception {
        cargarCarrito(usuario.getUsername(), model);
        return "carrito";
    }

    @GetMapping("/carrito/panel")
    public String panelCarrito(@AuthenticationPrincipal UserDetails usuario, Model model) throws Exception {
        cargarCarrito(usuario.getUsername(), model);
        return "fragments/cart-drawer :: content";
    }

    @PostMapping("/carrito/items")
    public String agregar(@AuthenticationPrincipal UserDetails usuario,
            @RequestParam String productoId, @RequestParam(defaultValue = "1") int cantidad,
            @RequestHeader(name = "X-Requested-With", required = false) String requestedWith,
            Model model, RedirectAttributes redirect) throws Exception {
        try {
            ordenServicio.agregarAlCarrito(productoId, cantidad, usuario.getUsername());
            if (esAjax(requestedWith)) {
                model.addAttribute("mensaje", "Producto agregado al carrito");
            } else {
                redirect.addFlashAttribute("mensaje", "Producto agregado al carrito");
            }
        } catch (Exception ex) {
            if (esAjax(requestedWith)) {
                model.addAttribute("error", ex.getMessage());
            } else {
                redirect.addFlashAttribute("error", ex.getMessage());
            }
        }
        if (esAjax(requestedWith)) {
            cargarCarrito(usuario.getUsername(), model);
            return "fragments/cart-drawer :: content";
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/items/{detalleId}/eliminar")
    public String eliminar(@AuthenticationPrincipal UserDetails usuario, @PathVariable String detalleId,
            @RequestHeader(name = "X-Requested-With", required = false) String requestedWith,
            Model model, RedirectAttributes redirect) throws Exception {
        try {
            ordenServicio.eliminarDelCarrito(detalleId, usuario.getUsername());
        } catch (Exception ex) {
            if (esAjax(requestedWith)) {
                model.addAttribute("error", ex.getMessage());
            } else {
                redirect.addFlashAttribute("error", ex.getMessage());
            }
        }
        if (esAjax(requestedWith)) {
            cargarCarrito(usuario.getUsername(), model);
            return "fragments/cart-drawer :: content";
        }
        return "redirect:/carrito";
    }

    private void cargarCarrito(String email, Model model) throws Exception {
        OrdenCompra carrito = ordenServicio.obtenerCarrito(email);
        model.addAttribute("carrito", carrito);
        model.addAttribute("lineas", carrito.getDetalles().stream()
                .filter(detalle -> !detalle.isEliminado())
                .toList());
    }

    private boolean esAjax(String requestedWith) {
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
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
            @RequestParam(name = "collection_id", required = false) String collectionId,
            @RequestParam(name = "status", required = false) String estadoInformado,
            @RequestParam(name = "collection_status", required = false) String collectionStatus,
            Model model) {
        model.addAttribute("orden", null);
        String idPago = paymentId;
        if (idPago == null || idPago.isBlank() || "null".equalsIgnoreCase(idPago)) {
            idPago = collectionId;
        }
        String estadoPago = estadoInformado;
        if (estadoPago == null || estadoPago.isBlank()) {
            estadoPago = collectionStatus;
        }
        model.addAttribute("estadoInformado", estadoPago);
        if (ordenId == null || ordenId.isBlank()) {
            model.addAttribute("mensaje", "No recibimos una referencia de orden desde Mercado Pago.");
            return "pago-resultado";
        }

        try {
            OrdenCompra orden = checkoutService.procesarRetorno(ordenId, idPago, usuario.getUsername());
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
            logger.error("No se pudo verificar el retorno del pago de la orden {}", ordenId, ex);
            model.addAttribute("mensaje", "No pudimos verificar el pago. Revisá el estado desde tu carrito.");
        }
        return "pago-resultado";
    }
}