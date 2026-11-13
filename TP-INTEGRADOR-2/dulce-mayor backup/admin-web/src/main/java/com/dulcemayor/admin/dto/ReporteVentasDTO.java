package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Histórico de ventas tal como lo devuelve la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteVentasDTO {

    private String desde;
    private String hasta;
    private String departamentoFiltrado;
    private String preventistaFiltrado;
    private String generadoEn;
    private String generadoPor;
    private List<VentaReporteDTO> ventas = new ArrayList<>();
    private List<SubtotalVentasDTO> porDepartamento = new ArrayList<>();
    private List<SubtotalVentasDTO> porPreventista = new ArrayList<>();
    private List<SubtotalVentasDTO> porDepartamentoYPreventista = new ArrayList<>();
    private long cantidadPedidos;
    private long cantidadClientes;
    private double totalGeneral;

    public String getGeneradoEnLegible() {
        return FechasLegibles.fechaHora(generadoEn);
    }
}
