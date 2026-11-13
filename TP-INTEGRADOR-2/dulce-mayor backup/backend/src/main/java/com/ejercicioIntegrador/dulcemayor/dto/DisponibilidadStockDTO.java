package com.ejercicioIntegrador.dulcemayor.dto;

/** Resultado de hayStockSuficiente(idProducto, cantidad). */
public record DisponibilidadStockDTO(String idProducto, int cantidadSolicitada, int cantidadDisponible,
                                     boolean disponible) {
}
