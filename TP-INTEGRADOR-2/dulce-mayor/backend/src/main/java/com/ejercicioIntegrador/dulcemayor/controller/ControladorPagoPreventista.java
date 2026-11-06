package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.PagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioComprobante;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Pagos y comprobante vistos por el preventista dueño del pedido. El ownership lo valida
 * {@link ServicioPedido#obtenerPedidoPropio} antes de cualquier operación.
 */
@RestController
@RequestMapping("/api/preventista/pedidos")
public class ControladorPagoPreventista {

    private final ServicioPago servicioPago;
    private final ServicioPedido servicioPedido;
    private final ServicioComprobante servicioComprobante;

    public ControladorPagoPreventista(ServicioPago servicioPago, ServicioPedido servicioPedido,
                                      ServicioComprobante servicioComprobante) {
        this.servicioPago = servicioPago;
        this.servicioPedido = servicioPedido;
        this.servicioComprobante = servicioComprobante;
    }

    @GetMapping("/{idPedido}/pagos")
    public ResponseEntity<List<PagoResponseDTO>> listar(@PathVariable String idPedido, Authentication authentication)
            throws MiException {
        servicioPedido.obtenerPedidoPropio(idPedido, authentication.getName());
        return ResponseEntity.ok(servicioPago.listarPorPedido(idPedido));
    }

    @PostMapping("/{idPedido}/pagos")
    public ResponseEntity<PagoResponseDTO> iniciar(@PathVariable String idPedido,
                                                    @RequestBody PagoRequestDTO dto,
                                                    Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPago.iniciarPorPreventista(idPedido, dto, authentication.getName()));
    }

    @GetMapping("/{idPedido}/comprobante")
    public ResponseEntity<String> comprobante(@PathVariable String idPedido, Authentication authentication)
            throws MiException {
        servicioPedido.obtenerPedidoPropio(idPedido, authentication.getName());
        return descargar(servicioComprobante.obtenerHtml(idPedido));
    }

    static ResponseEntity<String> descargar(ServicioComprobante.ComprobanteHtml documento) {
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documento.nombreArchivo() + "\"")
                .body(documento.html());
    }
}
