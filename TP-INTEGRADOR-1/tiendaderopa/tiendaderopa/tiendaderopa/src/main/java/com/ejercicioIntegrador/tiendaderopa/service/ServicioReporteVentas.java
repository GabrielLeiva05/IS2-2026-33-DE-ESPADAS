package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ReporteVentasDTO;
import com.ejercicioIntegrador.tiendaderopa.dto.reportes.VentaDetalleDTO;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServicioReporteVentas {

    @Autowired
    private ServicioDetalleCompra svcDetalleCompra;

    public ReporteVentasDTO generarReporte(LocalDate fechaDesde, LocalDate fechaHasta) throws Exception {
        validar(fechaDesde, fechaHasta);

        Date desde = aInicioDeDia(fechaDesde);
        Date hasta = aFinDeDia(fechaHasta);

        List<DetalleCompra> detalles = svcDetalleCompra.buscarParaReporteVentas(desde, hasta);

        List<VentaDetalleDTO> detalleDTO = detalles.stream()
                .map(this::aDTO)
                .collect(Collectors.toList());

        Set<String> ordenesDistintas = detalles.stream()
                .map(d -> d.getOrdenCompra().getId())
                .collect(Collectors.toSet());

        return new ReporteVentasDTO(fechaDesde, fechaHasta, detalleDTO, ordenesDistintas.size());
    }

    private void validar(LocalDate fechaDesde, LocalDate fechaHasta) throws Exception {
        if (fechaDesde == null || fechaHasta == null) {
            throw new Exception("Debe indicar fecha desde y fecha hasta");
        }
        if (fechaHasta.isBefore(fechaDesde)) {
            throw new Exception("La fecha hasta no puede ser anterior a la fecha desde");
        }
    }

    private VentaDetalleDTO aDTO(DetalleCompra detalle) {
        OrdenCompra orden = detalle.getOrdenCompra();
        Producto producto = detalle.getProducto();
        SubCategoria subCategoria = producto.getSubCategoria();

        return new VentaDetalleDTO(
                orden.getId(),
                orden.getIdentificadorCompra(),
                orden.getFecha(),
                producto.getNombre(),
                subCategoria != null && subCategoria.getCategoria() != null
                        ? subCategoria.getCategoria().getNombre() : null,
                subCategoria != null ? subCategoria.getNombre() : null,
                detalle.getCantidad(),
                detalle.getSubtotal(),
                orden.getFacturaCliente().getFormaDePago().getTipoPago().name()
        );
    }

    private Date aInicioDeDia(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date aFinDeDia(LocalDate fecha) {
        return Date.from(fecha.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
    }
}