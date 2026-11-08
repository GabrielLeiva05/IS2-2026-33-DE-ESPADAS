package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/**
 * Datos del gráfico de barras "pedidos por preventista por día" (HU-43) para un mes.
 * dias: números de día del mes (1..N). Cada serie tiene una cantidad por día, en el mismo orden.
 * No se cuentan los pedidos anulados.
 */
public record PedidosPorPreventistaDTO(String mes, List<Integer> dias, List<Serie> series, long total) {

    public record Serie(String idPreventista, String preventista, List<Long> cantidades, long total) {
    }
}
