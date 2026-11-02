package com.dulcemayor.admin.dto;

/** Porción del gráfico de dona "productos publicados por categoría". */
public record SegmentoDTO(String nombre, long cantidad, String color) {

    public String getNombre() { return nombre; }
    public long getCantidad() { return cantidad; }
    public String getColor() { return color; }
}
