package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Pedido tal como lo devuelve la API (resumen para administración). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoDTO {

    private String id;
    private long numeroPedido;
    private String fechaHoraCarga;
    private String fechaConfirmacion;
    private String fechaDespacho;
    private double total;
    private String estado;
    private String nombreFantasia;
    private String direccion;
    private String nombreCliente;
    private String nombrePreventista;
    private String idDistribuidor;
    private String nombreDistribuidor;

    /** "2026-11-03T10:15:30" -> "03/11 10:15". */
    public String getCargaLegible() {
        if (fechaHoraCarga == null || fechaHoraCarga.length() < 16) {
            return "";
        }
        return fechaHoraCarga.substring(8, 10) + "/" + fechaHoraCarga.substring(5, 7) + " " + fechaHoraCarga.substring(11, 16);
    }
}
