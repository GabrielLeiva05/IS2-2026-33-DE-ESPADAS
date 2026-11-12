package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.PedidosPorPreventistaDTO;
import com.dulcemayor.admin.dto.StockCategoriaDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/** Datos de los gráficos del panel. La agregación la hace el backend; acá solo se piden. */
@Service
public class ServicioEstadisticas {

    private final ClienteApi clienteApi;

    public ServicioEstadisticas(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<StockCategoriaDTO> stockPorCategoria(String token) {
        return clienteApi.listar(token, StockCategoriaDTO[].class, "/api/estadisticas/stock-por-categoria");
    }

    public PedidosPorPreventistaDTO pedidosPorPreventista(String token, String mes) {
        return clienteApi.obtener(token, PedidosPorPreventistaDTO.class,
                "/api/estadisticas/pedidos-por-preventista?mes={mes}", mes == null ? "" : mes);
    }
}
