package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Línea de un pedido del cliente. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineaPedidoClienteDTO {

    private String nombreProducto;
    private int cantidadPresentaciones;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
