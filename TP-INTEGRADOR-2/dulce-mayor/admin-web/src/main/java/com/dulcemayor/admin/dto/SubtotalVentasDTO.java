package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Subtotal de ventas tal como lo devuelve la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubtotalVentasDTO {

    private String departamento;
    private String preventista;
    private long cantidadPedidos;
    private double total;
}
