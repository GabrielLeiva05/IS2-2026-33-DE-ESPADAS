package com.dulcemayor.institucional.service;

import com.dulcemayor.institucional.cliente.ClienteApi;
import com.dulcemayor.institucional.dto.PedidoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Pedidos del cliente que inició sesión. La API identifica al cliente por el token: aunque alguien cambie
 * el identificador en la URL, el backend responde 404 si el pedido no es suyo.
 */
@Service
public class ServicioPedidos {

    private static final String BASE = "/api/pedidos/mis-pedidos";

    private final ClienteApi clienteApi;

    public ServicioPedidos(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<PedidoDTO> misPedidos(String token) {
        return clienteApi.listar(token, PedidoDTO[].class, BASE);
    }

    public PedidoDTO miPedido(String token, String id) {
        return clienteApi.obtener(token, PedidoDTO.class, BASE + "/{id}", id);
    }

    public PedidoDTO confirmar(String token, String id) {
        return clienteApi.enviar(token, Map.of(), PedidoDTO.class, BASE + "/{id}/confirmar", id);
    }

    public PedidoDTO anular(String token, String id) {
        return clienteApi.enviar(token, Map.of(), PedidoDTO.class, BASE + "/{id}/anular", id);
    }
}
