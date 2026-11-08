package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.FilaReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoSerieDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteVentasDTO;
import com.ejercicioIntegrador.dulcemayor.service.ServicioReporte;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reportes y estadísticas para administración. Las exportaciones son CSV compatible con Excel
 * (UTF-8 con BOM) y la versión imprimible se resuelve desde el panel con la impresión del navegador.
 */
@RestController
@RequestMapping("/api/reportes")
public class ControladorReporte {

    private final ServicioReporte servicioReporte;

    public ControladorReporte(ServicioReporte servicioReporte) {
        this.servicioReporte = servicioReporte;
    }

    @GetMapping("/ventas")
    public ResponseEntity<ReporteVentasDTO> ventas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "departamento") String agrupacion) {
        return ResponseEntity.ok(servicioReporte.ventas(desde, hasta, agrupacion));
    }

    @GetMapping(value = "/ventas.csv", produces = "text/csv")
    public ResponseEntity<byte[]> ventasCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "departamento") String agrupacion) {
        ReporteVentasDTO reporte = servicioReporte.ventas(desde, hasta, agrupacion);
        StringBuilder csv = new StringBuilder();
        csv.append("Periodo;").append(reporte.desde()).append(" a ").append(reporte.hasta()).append("\r\n");
        csv.append("Agrupacion;").append(reporte.agrupacion()).append("\r\n\r\n");
        csv.append("Grupo;Pedidos;Presentaciones;Importe\r\n");
        for (FilaReporteDTO fila : reporte.filas()) {
            csv.append(escapar(fila.grupo())).append(';')
                    .append(fila.pedidos()).append(';')
                    .append(fila.presentaciones()).append(';')
                    .append(formatearImporte(fila.importe())).append("\r\n");
        }
        csv.append("\r\nTOTAL;").append(reporte.pedidos()).append(';')
                .append(reporte.presentaciones()).append(';')
                .append(formatearImporte(reporte.importeTotal())).append("\r\n");
        // BOM para que Excel respete los acentos
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] cuerpo = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] salida = new byte[bom.length + cuerpo.length];
        System.arraycopy(bom, 0, salida, 0, bom.length);
        System.arraycopy(cuerpo, 0, salida, bom.length, cuerpo.length);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ventas-" + reporte.desde() + "-a-" + reporte.hasta() + ".csv\"")
                .body(salida);
    }

    @GetMapping("/pedidos-por-preventista")
    public ResponseEntity<List<PuntoSerieDTO>> porPreventista(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "false") boolean porMes) {
        return ResponseEntity.ok(servicioReporte.pedidosPorPreventista(desde, hasta, porMes));
    }

    @GetMapping("/stock")
    public ResponseEntity<List<Map<String, Object>>> stock() {
        return ResponseEntity.ok(servicioReporte.stockPorProducto());
    }

    private static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace(";", ",").replace("\r", " ").replace("\n", " ");
    }

    private static String formatearImporte(BigDecimal valor) {
        return valor == null ? "0.00" : valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
