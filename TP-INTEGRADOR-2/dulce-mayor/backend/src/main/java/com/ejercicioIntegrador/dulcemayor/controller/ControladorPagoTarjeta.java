package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.ConfirmarPagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoTarjetaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cobro con tarjeta por Checkout API de Mercado Pago: el navegador tokeniza la tarjeta (Bricks) y
 * acá se crea el pago. No hay redirección a otra página en ningún momento del flujo.
 */
@RestController
@RequestMapping("/api/preventista/pedidos")
public class ControladorPagoTarjeta {

    private final ServicioPago servicioPago;
    private final com.ejercicioIntegrador.dulcemayor.service.ServicioPedido servicioPedido;

    public ControladorPagoTarjeta(ServicioPago servicioPago,
                                  com.ejercicioIntegrador.dulcemayor.service.ServicioPedido servicioPedido) {
        this.servicioPago = servicioPago;
        this.servicioPedido = servicioPedido;
    }

    /** Cobra el pago pendiente del pedido con la tarjeta tokenizada. */
    @PostMapping("/{idPedido}/pagos/{idPago}/tarjeta")
    public ResponseEntity<PagoResponseDTO> cobrar(@PathVariable String idPedido, @PathVariable String idPago,
                                                  @RequestBody PagoTarjetaRequestDTO dto,
                                                  Authentication authentication) throws MiException {
        // Autorización por ownership: el pedido debe pertenecer al preventista autenticado
        servicioPedido.obtenerPedidoPropio(idPedido, authentication.getName());
        return ResponseEntity.ok(servicioPago.cobrarConTarjeta(idPago, dto));
    }
}
