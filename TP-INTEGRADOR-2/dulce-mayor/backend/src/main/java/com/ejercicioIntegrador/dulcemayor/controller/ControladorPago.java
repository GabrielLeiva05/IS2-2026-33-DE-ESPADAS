package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.ConfirmarPagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Conciliación de pagos (personal administrativo) y notificación del proveedor.
 * La notificación de Mercado Pago es el único endpoint abierto: se autentica con la firma HMAC.
 */
@RestController
@RequestMapping("/api/pagos")
public class ControladorPago {

    private final ServicioPago servicioPago;

    public ControladorPago(ServicioPago servicioPago) {
        this.servicioPago = servicioPago;
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<List<PagoResponseDTO>> listar(@PathVariable String idPedido) {
        return ResponseEntity.ok(servicioPago.listarPorPedido(idPedido));
    }

    @PostMapping("/{idPago}/acreditar")
    public ResponseEntity<PagoResponseDTO> acreditar(@PathVariable String idPago,
                                                      @RequestBody(required = false) ConfirmarPagoRequestDTO dto)
            throws MiException {
        return ResponseEntity.ok(servicioPago.acreditar(idPago, dto));
    }

    @PostMapping("/{idPago}/rechazar")
    public ResponseEntity<PagoResponseDTO> rechazar(@PathVariable String idPago,
                                                     @RequestBody(required = false) ConfirmarPagoRequestDTO dto)
            throws MiException {
        return ResponseEntity.ok(servicioPago.rechazar(idPago, dto));
    }

    /**
     * Webhook de Mercado Pago. Nunca se confía en el cuerpo: se valida la firma y se consulta el
     * pago server-to-server. Responde 401 si la firma no es válida para que el proveedor reintente
     * solo cuando corresponde.
     */
    @PostMapping("/mercadopago/notificacion")
    public ResponseEntity<Void> notificacionMercadoPago(
            @RequestHeader(value = "x-signature", required = false) String firma,
            @RequestHeader(value = "x-request-id", required = false) String requestId,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "type", required = false) String tipo) {
        try {
            servicioPago.procesarNotificacion(firma, requestId, dataId, tipo);
            return ResponseEntity.ok().build();
        } catch (MiException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
