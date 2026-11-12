package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Venta del histórico tal como la devuelve la API (VentaDTO). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VentaReporteDTO {

    private String departamento;
    private String preventista;
    private String idCliente;
    private String numeroDocumentoCliente;
    private String nombreCliente;
    private String nombreComercio;
    private long numeroPedido;
    private String fechaVenta;
    private double subtotal;
    private double descuento;
    private double total;

    public String getFechaLegible() {
        return FechasLegibles.fechaHora(fechaVenta);
    }
}
