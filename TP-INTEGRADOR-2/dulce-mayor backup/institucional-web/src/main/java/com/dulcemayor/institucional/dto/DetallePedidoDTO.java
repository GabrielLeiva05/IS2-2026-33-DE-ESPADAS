package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Renglón de un pedido: el precio unitario es el vigente al momento de la carga. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetallePedidoDTO {

    private String nombreProducto;
    private String codigoProducto;
    private String presentacion;
    private int unidadesPorPresentacion;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    /** "CAJA" -> "Caja x 24". */
    public String getPresentacionLegible() {
        if (presentacion == null || presentacion.isBlank()) {
            return "";
        }
        String texto = presentacion.charAt(0) + presentacion.substring(1).toLowerCase().replace('_', ' ');
        return unidadesPorPresentacion > 1 ? texto + " x " + unidadesPorPresentacion : texto;
    }
}
