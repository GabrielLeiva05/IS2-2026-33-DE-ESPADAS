package com.ejercicioIntegrador.tiendaderopa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductoOfertaDTO {
    private String nombre;
    private String descripcion;
    private String talle;
    private String categoria;   // ej: "Mujeres · Calzado"
    private double precio;      // precio vigente
}