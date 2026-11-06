package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.PagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioComprobante;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Bandeja, pagos y cierre autorizado de pedidos (personal administrativo; el jefe lo hereda).
 * El consumo de stock ya no se hace a mano: exige el pago total acreditado.
 */
@RestController
@RequestMapping("/api/pedidos")
public class ControladorPedidoAdministrativo {

    private final ServicioPedido servicioPedido;
    private final ServicioPago servicioPago;
    private final ServicioComprobante servicioComprobante;

    public ControladorPedidoAdministrativo(ServicioPedido servicioPedido, ServicioPago servicioPago,
                                           ServicioComprobante servicioComprobante) {
        this.servicioPedido = servicioPedido;
        this.servicioPago = servicioPago;
        this.servicioComprobante = servicioComprobante;
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar() {
        return ResponseEntity.ok(servicioPedido.listarTodos());
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> detalle(@PathVariable String idPedido) throws MiException {
        return ResponseEntity.ok(servicioPedido.obtenerDetalle(idPedido));
    }

    @PostMapping("/{idPedido}/pagos")
    public ResponseEntity<PagoResponseDTO> iniciarPago(@PathVariable String idPedido,
                                                        @RequestBody PagoRequestDTO dto) throws MiException {
        return ResponseEntity.ok(servicioPago.iniciarPorAdministrativo(idPedido, dto));
    }

    @PostMapping("/{idPedido}/pagos/efectivo")
    public ResponseEntity<PagoResponseDTO> registrarEfectivo(@PathVariable String idPedido) throws MiException {
        return ResponseEntity.ok(servicioPago.registrarEfectivo(idPedido));
    }

    @PostMapping("/{idPedido}/completar")
    public ResponseEntity<PedidoResponseDTO> completar(@PathVariable String idPedido) throws MiException {
        return ResponseEntity.ok(servicioPedido.completarAdministrativo(idPedido));
    }

    @PostMapping("/{idPedido}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable String idPedido) throws MiException {
        return ResponseEntity.ok(servicioPedido.cancelarAdministrativo(idPedido));
    }

    @GetMapping("/{idPedido}/comprobante")
    public ResponseEntity<String> comprobante(@PathVariable String idPedido) throws MiException {
        servicioPedido.obtenerPedido(idPedido);
        return ControladorPagoPreventista.descargar(servicioComprobante.obtenerHtml(idPedido));
    }
}
