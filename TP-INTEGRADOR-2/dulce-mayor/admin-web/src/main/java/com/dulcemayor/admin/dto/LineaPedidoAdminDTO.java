package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Línea de un pedido tal como la devuelve la API (PedidoLineaResponseDTO). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineaPedidoAdminDTO {

    private String idProducto;
    private String nombreProducto;
    private int cantidadPresentaciones;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
