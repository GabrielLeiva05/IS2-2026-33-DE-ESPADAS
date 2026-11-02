package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Stock de un producto (/api/stock). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDTO {

    private String id;
    private String idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private int cantidadActual;
    private int stockMinimo;
    private boolean bajoMinimo;

    /** Porcentaje del stock mínimo que queda (0 a 100), para la barra del panel. */
    public int getPorcentajeDelMinimo() {
        if (stockMinimo <= 0) {
            return cantidadActual > 0 ? 100 : 0;
        }
        return Math.max(0, Math.min(100, cantidadActual * 100 / stockMinimo));
    }
}
