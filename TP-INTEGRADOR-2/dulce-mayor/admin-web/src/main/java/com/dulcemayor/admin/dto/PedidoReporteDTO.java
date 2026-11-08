package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fila del reporte de clientes y pedidos tal como la devuelve la API (PedidoReporteDTO). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoReporteDTO {

    private String idPuntoVenta;
    private String idCliente;
    private String numeroDocumentoCliente;
    private String nombreCliente;
    private String correoCliente;
    private String nombreComercio;
    private String departamento;
    private String domicilioComercio;
    private long numeroPedido;
    private String fechaHoraCarga;
    private String preventista;
    private String estado;
    private String descripcionEstado;
    private String estadoEnvio;
    private String distribuidor;
    private String fechaFacturacion;
    private String fechaEntrega;
    private Double montoFacturado;

    public String getCargaLegible() {
        return FechasLegibles.fechaHora(fechaHoraCarga);
    }

    public String getFacturacionLegible() {
        return FechasLegibles.fecha(fechaFacturacion);
    }

    public String getEntregaLegible() {
        return FechasLegibles.fechaHora(fechaEntrega);
    }
}
