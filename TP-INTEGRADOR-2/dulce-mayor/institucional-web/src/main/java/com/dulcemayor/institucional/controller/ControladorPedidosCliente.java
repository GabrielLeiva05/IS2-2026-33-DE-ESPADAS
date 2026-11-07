package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.seguridad.GestorSesion;
import com.dulcemayor.institucional.service.ServicioPedidosCliente;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;

/** Portal del cliente: consulta de sus pedidos, estados y comprobantes. */
@Controller
@RequestMapping("/mis-pedidos")
public class ControladorPedidosCliente {

    private final ServicioPedidosCliente servicioPedidos;
    private final GestorSesion gestorSesion;

    public ControladorPedidosCliente(ServicioPedidosCliente servicioPedidos, GestorSesion gestorSesion) {
        this.servicioPedidos = servicioPedidos;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(HttpServletRequest request, Model model) {
        model.addAttribute("pedidos", servicioPedidos.listar(token(request)));
        return "mis-pedidos";
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
