package com.dulcemayor.institucional.service;

import com.dulcemayor.institucional.cliente.ClienteApi;
import com.dulcemayor.institucional.dto.PedidoClienteDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/** Consulta de pedidos y comprobantes del cliente autenticado (solo lectura). */
@Service
public class ServicioPedidosCliente {

    private final ClienteApi clienteApi;

    public ServicioPedidosCliente(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<PedidoClienteDTO> listar(String token) {
        return clienteApi.listar(token, PedidoClienteDTO[].class, "/api/cliente/pedidos");
    }

    public String comprobanteHtml(String token, String idPedido) {
        return clienteApi.obtenerTexto(token, "/api/cliente/pedidos/{id}/comprobante", idPedido);
    }
}
