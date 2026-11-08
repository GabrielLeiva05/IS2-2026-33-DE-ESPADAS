package com.dulcemayor.admin.dto;

/** Porción del gráfico de torta (GET /api/estadisticas/stock-por-categoria). */
public record StockCategoriaDTO(String categoria, long bultos, long productos) {
}
