package com.dulcemayor.admin.dto;

/** Tarjeta de indicador del panel (valor calculado a partir de datos reales de la API). */
public record IndicadorDTO(String etiqueta, String valor, String detalle, String icono, String tono, String enlace) {

    public String getEtiqueta() { return etiqueta; }
    public String getValor() { return valor; }
    public String getDetalle() { return detalle; }
    public String getIcono() { return icono; }
    public String getTono() { return tono; }
    public String getEnlace() { return enlace; }
}
