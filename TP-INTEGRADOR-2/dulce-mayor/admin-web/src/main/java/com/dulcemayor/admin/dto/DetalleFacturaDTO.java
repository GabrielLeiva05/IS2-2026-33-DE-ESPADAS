package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetalleFacturaDTO {

    private String nombreProducto;
    private String codigoProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
}
