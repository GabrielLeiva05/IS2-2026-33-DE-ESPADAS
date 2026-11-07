package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioComprobante;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Portal del cliente: solo consulta los pedidos de sus propios comercios (identificados por el
 * correo de contacto del punto de venta) y el comprobante cuando el pago está acreditado.
 */
@RestController
@RequestMapping("/api/cliente")
public class ControladorClientePedidos {

    private final ServicioPedido servicioPedido;
    private final ServicioComprobante servicioComprobante;

    public ControladorClientePedidos(ServicioPedido servicioPedido, ServicioComprobante servicioComprobante) {
        this.servicioPedido = servicioPedido;
        this.servicioComprobante = servicioComprobante;
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listar(Authentication authentication) {
        return ResponseEntity.ok(servicioPedido.listarPedidosDelCliente(authentication.getName()));
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<PedidoResponseDTO> detalle(@PathVariable String id, Authentication authentication)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.obtenerPedidoDelCliente(id, authentication.getName()));
    }

    @GetMapping("/pedidos/{id}/comprobante")
    public ResponseEntity<String> comprobante(@PathVariable String id, Authentication authentication)
            throws MiException {
        servicioPedido.obtenerPedidoDelCliente(id, authentication.getName());
        return ControladorPagoPreventista.descargar(servicioComprobante.obtenerHtml(id));
    }
}
