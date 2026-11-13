package com.dulcemayor.admin.dto;

import java.util.List;

/** Datos del gráfico de barras (GET /api/estadisticas/pedidos-por-preventista). */
public record PedidosPorPreventistaDTO(String mes, List<Integer> dias, List<Serie> series, long total) {

    public record Serie(String idPreventista, String preventista, List<Long> cantidades, long total) {
    }
}
