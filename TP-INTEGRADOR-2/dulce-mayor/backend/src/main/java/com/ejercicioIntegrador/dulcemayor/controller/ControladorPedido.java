package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.Authentication;
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

    public ControladorPedido(ServicioPedido servicioPedido) {
        this.servicioPedido = servicioPedido;
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

    @PostMapping("/{idPedido}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmar(@PathVariable String idPedido,
                                                        Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPedido.confirmar(idPedido, authentication.getName()));
    }

    @PostMapping("/{idPedido}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable String idPedido,
                                                       Authentication authentication) throws MiException {
        return ResponseEntity.ok(servicioPedido.cancelar(idPedido, authentication.getName()));
    }
}
