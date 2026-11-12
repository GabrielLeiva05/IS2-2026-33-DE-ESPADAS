package com.ejercicioIntegrador.dulcemayor.dto;

/**
 * Una porción del gráfico de torta del stock (HU-43): categoría real del catálogo, bultos en stock
 * (suma de Stock.cantidadActual, que se cuenta en la presentación de cada producto) y cantidad de productos.
 */
public record StockCategoriaDTO(String categoria, long bultos, long productos) {
}
