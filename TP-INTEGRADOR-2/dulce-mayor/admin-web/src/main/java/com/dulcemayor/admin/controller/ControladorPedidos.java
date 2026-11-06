package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioPedidosAdmin;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Bandeja de pedidos, detalle con pagos y conciliación de transferencias. */
@Controller
@RequestMapping("/pedidos")
public class ControladorPedidos {

    private static final List<String> ESTADOS = List.of("BORRADOR", "CONFIRMADO", "COMPLETADO", "CANCELADO");

    private final ServicioPedidosAdmin servicioPedidos;
    private final GestorSesion gestorSesion;

    public ControladorPedidos(ServicioPedidosAdmin servicioPedidos, GestorSesion gestorSesion) {
        this.servicioPedidos = servicioPedidos;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String estado,
                         @RequestParam(required = false) String busqueda,
                         HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("pedidos", servicioPedidos.listar(token, estado, busqueda));
        model.addAttribute("estados", ESTADOS);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("busqueda", busqueda);
        return "pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("pedido", servicioPedidos.detalle(token, id));
        model.addAttribute("pagos", servicioPedidos.pagos(token, id));
        model.addAttribute("tieneComprobante", servicioPedidos.tieneComprobante(token, id));
        return "pedidos/detalle";
    }

    @PostMapping("/{id}/pagos/{idPago}/acreditar")
    public String acreditar(@PathVariable String id, @PathVariable String idPago,
                            @RequestParam(required = false) String referencia,
                            @RequestParam(required = false) String observacion,
                            HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            servicioPedidos.acreditar(token(request), idPago, referencia, observacion);
            redireccion.addFlashAttribute("exito", "Pago acreditado: se consumió el stock y se emitió el comprobante.");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/{id}/pagos/{idPago}/rechazar")
    public String rechazar(@PathVariable String id, @PathVariable String idPago,
                           @RequestParam(required = false) String observacion,
                           HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            servicioPedidos.rechazar(token(request), idPago, observacion);
            redireccion.addFlashAttribute("exito", "El pago quedó marcado como rechazado.");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/{id}/pagos")
    public String iniciarPago(@PathVariable String id, @RequestParam String medio,
                              HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            var creado = servicioPedidos.iniciarPago(token(request), id, medio);
            redireccion.addFlashAttribute("exito", "Cobro iniciado con " + creado.getMedioLegible()
                    + ". El estado del pago quedó en " + creado.getEstadoLegible() + ".");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/{id}/efectivo")
    public String registrarEfectivo(@PathVariable String id, HttpServletRequest request,
                                    RedirectAttributes redireccion) {
        try {
            servicioPedidos.registrarEfectivo(token(request), id);
            redireccion.addFlashAttribute("exito", "Cobro en efectivo registrado y pedido completado.");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable String id, HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            servicioPedidos.cancelar(token(request), id);
            redireccion.addFlashAttribute("exito", "Pedido cancelado y reserva de stock liberada.");
        } catch (RuntimeException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @GetMapping("/{id}/comprobante")
    public ResponseEntity<byte[]> comprobante(@PathVariable String id, HttpServletRequest request) {
        String html = servicioPedidos.comprobanteHtml(token(request), id);
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobante-" + id + ".html\"")
                .body(html.getBytes(StandardCharsets.UTF_8));
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
