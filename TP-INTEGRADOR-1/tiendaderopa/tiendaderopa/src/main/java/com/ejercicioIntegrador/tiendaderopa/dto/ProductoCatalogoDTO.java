package com.ejercicioIntegrador.tiendaderopa.dto;

import com.ejercicioIntegrador.tiendaderopa.model.Producto;

public record ProductoCatalogoDTO(Producto producto, double precio, int stockDisponible) {
}