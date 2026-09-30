package com.ejercicioIntegrador.tiendaderopa.dto.reportes;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

/*
    DTO para mostrar el reporte de ventas.
    Trabaja con ServicioReporteVentas para generar los reportes de ventas.
*/

@Getter
@Setter
public class ReporteVentasDTO {

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private double totalVentas;
    private int cantidadOperaciones;
    private List<VentaDetalleDTO> detalle;

    public ReporteVentasDTO(LocalDate fechaDesde, LocalDate fechaHasta,
                             List<VentaDetalleDTO> detalle, int cantidadOperaciones) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.detalle = detalle;
        this.cantidadOperaciones = cantidadOperaciones;
        this.totalVentas = detalle == null ? 0 : detalle.stream()
                .mapToDouble(VentaDetalleDTO::getSubtotal)
                .sum();
    }
}
