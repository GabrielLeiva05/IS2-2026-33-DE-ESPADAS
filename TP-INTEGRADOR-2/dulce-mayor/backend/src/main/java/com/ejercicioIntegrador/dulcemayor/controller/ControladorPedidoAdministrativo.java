package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ciclo autorizado de un pedido ya confirmado por el preventista: la finalización consume la
 * reserva de stock y la cancelación la libera. Solo personal administrativo (el jefe lo hereda).
 */
@RestController
@RequestMapping("/api/pedidos")
public class ControladorPedidoAdministrativo {

    private final ServicioPedido servicioPedido;

    public ControladorPedidoAdministrativo(ServicioPedido servicioPedido) {
        this.servicioPedido = servicioPedido;
    }

    @PostMapping("/{idPedido}/completar")
    public ResponseEntity<PedidoResponseDTO> completar(@PathVariable String idPedido) throws MiException {
        return ResponseEntity.ok(servicioPedido.completarAdministrativo(idPedido));
    }

    @PostMapping("/{idPedido}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable String idPedido) throws MiException {
        return ResponseEntity.ok(servicioPedido.cancelarAdministrativo(idPedido));
    }
}
