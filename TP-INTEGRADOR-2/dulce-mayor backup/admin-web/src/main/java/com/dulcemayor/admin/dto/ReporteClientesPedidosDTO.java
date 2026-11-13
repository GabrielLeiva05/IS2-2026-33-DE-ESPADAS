package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Reporte de clientes y pedidos tal como lo devuelve la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteClientesPedidosDTO {

    private String desde;
    private String hasta;
    private String departamentoFiltrado;
    private String estadoFiltrado;
    private String descripcionEstadoFiltrado;
    private String generadoEn;
    private String generadoPor;
    private List<PedidoReporteDTO> pedidos = new ArrayList<>();
    private long cantidadComercios;
    private long cantidadPedidos;
    private long cantidadEntregados;
    private long cantidadPendientesDeEntrega;
    private double totalFacturado;

    public String getGeneradoEnLegible() {
        return FechasLegibles.fechaHora(generadoEn);
    }
}
