package com.ejercicioIntegrador.dulcemayor.exportacion;

import com.ejercicioIntegrador.dulcemayor.dto.PedidoReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteClientesPedidosDTO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PATRÓN STRATEGY — ESTRATEGIA CONCRETA: reporte de clientes y pedidos en PDF, con JasperReports.
 *
 * Usa la plantilla reportes/reporte_clientes_pedidos.jrxml (agrupa por comercio, calcula subtotales por comercio
 * y el resumen general). Esta clase solo convierte el DTO en las filas y parámetros que espera la plantilla;
 * no consulta la base ni decide reglas de negocio. La plantilla se compila una sola vez, la primera vez que se usa.
 */
@Component
public class EstrategiaConcretaClientesPedidosPdf implements EstrategiaExportacion<ReporteClientesPedidosDTO> {

    private static final String PLANTILLA = "/reportes/reporte_clientes_pedidos.jrxml";
    private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");

    private volatile JasperReport plantillaCompilada;

    @Override
    public Class<ReporteClientesPedidosDTO> tipoDeReporte() {
        return ReporteClientesPedidosDTO.class;
    }

    @Override
    public FormatoExportacion formato() {
        return FormatoExportacion.PDF;
    }

    @Override
    public byte[] exportar(ReporteClientesPedidosDTO reporte) {
        try {
            JRMapCollectionDataSource filas = new JRMapCollectionDataSource(armarFilas(reporte));
            JasperPrint impresion = JasperFillManager.fillReport(plantilla(), armarParametros(reporte), filas);
            return JasperExportManager.exportReportToPdf(impresion);
        } catch (JRException e) {
            throw new ErrorExportacionException("No se pudo generar el PDF de clientes y pedidos", e);
        }
    }

    private Map<String, Object> armarParametros(ReporteClientesPedidosDTO reporte) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("PERIODO_DESDE", TextoReporte.fecha(reporte.desde(), "inicio"));
        parametros.put("PERIODO_HASTA", TextoReporte.fecha(reporte.hasta(), "hoy"));
        parametros.put("FECHA_GENERACION", TextoReporte.fechaHora(reporte.generadoEn()));
        parametros.put("USUARIO_GENERADOR", reporte.generadoPor() == null ? "-" : reporte.generadoPor());
        parametros.put("FILTROS", TextoReporte.filtros("Departamento", reporte.departamentoFiltrado(),
                "Estado", reporte.descripcionEstadoFiltrado()));
        parametros.put(JRParameter.REPORT_LOCALE, LOCALE_AR);
        return parametros;
    }

    /** Una fila (mapa campo -> valor) por pedido, con los nombres de campo que declara la plantilla. */
    private List<Map<String, ?>> armarFilas(ReporteClientesPedidosDTO reporte) {
        List<Map<String, ?>> filas = new ArrayList<>();
        for (PedidoReporteDTO pedido : reporte.pedidos()) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("clavePuntoVenta", pedido.idPuntoVenta());
            fila.put("dniCliente", pedido.numeroDocumentoCliente());
            fila.put("nombreCliente", pedido.nombreCliente());
            fila.put("emailCliente", pedido.correoCliente());
            fila.put("nombreComercio", pedido.nombreComercio());
            fila.put("departamento", pedido.departamento());
            fila.put("domicilioComercio", pedido.domicilioComercio());
            fila.put("numeroPedido", String.valueOf(pedido.numeroPedido()));
            fila.put("fechaCarga", aFecha(pedido.fechaHoraCarga()));
            fila.put("preventista", pedido.preventista());
            fila.put("estadoPedido", pedido.descripcionEstado());
            fila.put("estadoEnvio", pedido.estadoEnvio());
            fila.put("fechaFacturacion", aFecha(pedido.fechaFacturacion()));
            fila.put("fechaEntrega", aFecha(pedido.fechaEntrega()));
            fila.put("nombreDistribuidor", pedido.distribuidor());
            fila.put("montoFacturado", pedido.montoFacturado());
            filas.add(fila);
        }
        return filas;
    }

    private static Date aFecha(LocalDateTime fecha) {
        return fecha == null ? null : Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant());
    }

    private JasperReport plantilla() throws JRException {
        JasperReport compilada = plantillaCompilada;
        if (compilada == null) {
            synchronized (this) {
                compilada = plantillaCompilada;
                if (compilada == null) {
                    try (InputStream entrada = EstrategiaConcretaClientesPedidosPdf.class.getResourceAsStream(PLANTILLA)) {
                        if (entrada == null) {
                            throw new JRException("No se encontró la plantilla " + PLANTILLA);
                        }
                        compilada = JasperCompileManager.compileReport(entrada);
                    } catch (IOException e) {
                        throw new JRException("No se pudo leer la plantilla " + PLANTILLA, e);
                    }
                    plantillaCompilada = compilada;
                }
            }
        }
        return compilada;
    }
}
