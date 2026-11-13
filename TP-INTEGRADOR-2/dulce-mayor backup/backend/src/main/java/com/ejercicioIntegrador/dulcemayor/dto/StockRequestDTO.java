package com.ejercicioIntegrador.dulcemayor.dto;

/** Datos de entrada para registrar el stock inicial de un producto (crearStock del diagrama). */
public record StockRequestDTO(String idProducto, Integer cantidadInicial, Integer stockMinimo) {
}
