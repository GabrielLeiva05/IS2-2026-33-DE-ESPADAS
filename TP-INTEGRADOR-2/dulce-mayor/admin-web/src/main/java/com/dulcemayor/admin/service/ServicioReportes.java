package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.FilaReporteAdminDTO;
import com.dulcemayor.admin.dto.PuntoSerieAdminDTO;
import com.dulcemayor.admin.dto.ReporteAdminDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reportes y estadísticas consumidos desde la API. */
@Service
public class ServicioReportes {

    private final ClienteApi clienteApi;

    public ServicioReportes(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public ReporteAdminDTO ventas(String token, String desde, String hasta, String agrupacion) {
        return clienteApi.obtener(token, ReporteAdminDTO.class,
                "/api/reportes/ventas?desde={desde}&hasta={hasta}&agrupacion={agrupacion}",
                desde == null ? "" : desde, hasta == null ? "" : hasta,
                agrupacion == null ? "departamento" : agrupacion);
    }

    public List<PuntoSerieAdminDTO> pedidosPorPreventista(String token, String desde, String hasta, boolean porMes) {
        return clienteApi.listar(token, PuntoSerieAdminDTO[].class,
                "/api/reportes/pedidos-por-preventista?desde={desde}&hasta={hasta}&porMes={porMes}",
                desde == null ? "" : desde, hasta == null ? "" : hasta, porMes);
    }

    public List<Map<String, Object>> stock(String token) {
        return clienteApi.listar(token, Map[].class, "/api/reportes/stock").stream()
                .map(fila -> (Map<String, Object>) new LinkedHashMap<>(fila))
                .toList();
    }

    /** Exportación CSV compatible con Excel. */
    public byte[] ventasCsv(String token, String desde, String hasta, String agrupacion) {
        return clienteApi.obtenerBytes(token,
                "/api/reportes/ventas.csv?desde={desde}&hasta={hasta}&agrupacion={agrupacion}",
                desde == null ? "" : desde, hasta == null ? "" : hasta,
                agrupacion == null ? "departamento" : agrupacion);
    }

    /** Totales para el panel de inicio (mes en curso). */
    public ReporteAdminDTO resumenDelMes(String token) {
        return ventas(token, null, null, "departamento");
    }

    public List<FilaReporteAdminDTO> topComercios(String token, String desde, String hasta) {
        ReporteAdminDTO reporte = ventas(token, desde, hasta, "cliente");
        return reporte.getFilas() == null ? List.of() : reporte.getFilas().stream().limit(5).toList();
    }
}
