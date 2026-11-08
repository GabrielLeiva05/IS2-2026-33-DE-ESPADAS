package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.AplicarPromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/preventista/pedidos")
public class ControladorPedido {

    private final ServicioPedido servicioPedido;
    private final ServicioPago servicioPago;

    public ControladorPedido(ServicioPedido servicioPedido, ServicioPago servicioPago) {
        this.servicioPedido = servicioPedido;
        this.servicioPago = servicioPago;
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar(Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPedido.listarMisPedidos(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crear(@RequestBody PedidoRequestDTO dto,
                                                     Authentication authentication) throws MiException {
        PedidoResponseDTO creado = servicioPedido.crearBorrador(dto, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{idPedido}/lineas")
    public ResponseEntity<PedidoResponseDTO> actualizar(@PathVariable String idPedido, @RequestBody PedidoRequestDTO dto,
                                                         Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPedido.actualizarBorrador(idPedido, dto, authentication.getName()));
    }

    @PostMapping("/{idPedido}/promocion")
    public ResponseEntity<PedidoResponseDTO> aplicarPromocion(@PathVariable String idPedido,
                                                              @RequestBody(required = false) AplicarPromocionRequestDTO dto,
                                                              Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPedido.aplicarPromocion(idPedido,
                dto == null ? null : dto.codigo(), authentication.getName()));
    }

    @DeleteMapping("/{idPedido}/promocion")
    public ResponseEntity<PedidoResponseDTO> quitarPromocion(@PathVariable String idPedido,
                                                              Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPedido.quitarPromocion(idPedido, authentication.getName()));
    }

    @PostMapping("/{idPedido}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmar(@PathVariable String idPedido,
                                                        @RequestBody(required = false) PagoRequestDTO cobro,
                                                        Authentication authentication) throws MiException {
        PedidoResponseDTO confirmado = servicioPedido.confirmar(idPedido, authentication.getName());
        if (cobro != null && cobro.medio() != null) {
            // El preventista confirma y elige cómo paga el cliente: con Mercado Pago el QR se
            // genera y se envía por correo en este mismo paso, sin redirigir al comprador.
            servicioPago.iniciarPorPreventista(idPedido, cobro, authentication.getName());
        }
        return ResponseEntity.ok(confirmado);
    }

    @PostMapping("/{idPedido}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable String idPedido,
                                                       Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPedido.cancelar(idPedido, authentication.getName()));
    }
}
