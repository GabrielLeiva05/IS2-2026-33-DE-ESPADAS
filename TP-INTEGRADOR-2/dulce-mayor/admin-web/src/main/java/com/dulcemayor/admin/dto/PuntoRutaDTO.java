package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Parada de una ruta con su resultado. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PuntoRutaDTO {

    private int orden;
    private String resultado;
    private String motivo;
    private String idPuntoVenta;
    private String nombreFantasia;
    private String direccion;
    private String nombreCliente;

    public String getResultadoLegible() {
        if (resultado == null) {
            return "";
        }
        return switch (resultado) {
            case "PENDIENTE" -> "Pendiente";
            case "CON_PEDIDO" -> "Con pedido";
            case "SIN_PEDIDO" -> "Sin pedido";
            case "ENTREGADO" -> "Entregado";
            case "NO_ENTREGADO" -> "No entregado";
            default -> resultado;
        };
    }
}
