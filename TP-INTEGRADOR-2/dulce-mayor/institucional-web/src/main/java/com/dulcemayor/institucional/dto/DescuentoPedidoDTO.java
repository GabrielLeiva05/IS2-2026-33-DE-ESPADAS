package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Copia de una promoción o código aplicado al pedido, tal como quedó al cargarlo. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DescuentoPedidoDTO {

    private String tipo;
    private String nombrePromocion;
    private String codigo;
    private BigDecimal porcentaje;
    private BigDecimal importe;

    public String getTitulo() {
        return codigo == null ? nombrePromocion : nombrePromocion + " (" + codigo + ")";
    }
}
