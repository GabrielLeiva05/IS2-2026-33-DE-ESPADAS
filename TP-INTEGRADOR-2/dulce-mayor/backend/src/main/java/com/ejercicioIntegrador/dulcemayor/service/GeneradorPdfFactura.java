package com.ejercicioIntegrador.dulcemayor.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * generarPdf(id) del diagrama. Adaptación del mini proyecto de facturación (JasperReports 6.20.6):
 *  - Las plantillas .jrxml están en resources/reportes y traen su propia consulta SQL sobre las tablas reales
 *    (facturas, detalles_factura, pedidos, puntos_venta, personas, direcciones, productos, proveedores).
 *  - El reporte se llena con una conexión del DataSource que administra Spring (el mismo de JPA): no hay
 *    credenciales en la plantilla ni en el código.
 *  - Cada plantilla se compila una sola vez y queda en memoria (el mini proyecto la compilaba en cada descarga).
 * Solo genera el documento: quién puede pedirlo lo deciden los servicios de facturas.
 */
@Component
public class GeneradorPdfFactura {

    public static final String PLANTILLA_CLIENTE = "reportes/factura_cliente.jrxml";
    public static final String PLANTILLA_PROVEEDOR = "reportes/factura_proveedor.jrxml";

    private static final Locale LOCALE_AR = Locale.of("es", "AR");
    private static final Logger LOG = LoggerFactory.getLogger(GeneradorPdfFactura.class);

    private final DataSource dataSource;
    private final Map<String, JasperReport> compiladas = new ConcurrentHashMap<>();

    public GeneradorPdfFactura(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Genera el PDF de una factura ya persistida.
     * @param plantilla PLANTILLA_CLIENTE o PLANTILLA_PROVEEDOR
     * @param idFactura id de la factura (parámetro ID_FACTURA de la consulta)
     * @param alicuotaIva alícuota usada, solo para la leyenda "IVA (21%)"
     */
    public byte[] generar(String plantilla, String idFactura, double alicuotaIva) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("ID_FACTURA", idFactura);
        parametros.put("ALICUOTA_IVA", alicuotaIva);
        // Importes y fechas con formato argentino ($ 8.650,00)
        parametros.put(JRParameter.REPORT_LOCALE, LOCALE_AR);
        try (Connection conexion = dataSource.getConnection()) {
            JasperPrint impreso = JasperFillManager.fillReport(compilada(plantilla), parametros, conexion);
            return JasperExportManager.exportReportToPdf(impreso);
        } catch (JRException | SQLException e) {
            LOG.error("No se pudo generar el PDF de la factura {} con {}", idFactura, plantilla, e);
            throw new IllegalStateException("No se pudo generar el PDF de la factura", e);
        }
    }

    private JasperReport compilada(String plantilla) {
        return compiladas.computeIfAbsent(plantilla, ruta -> {
            try (InputStream jrxml = new ClassPathResource(ruta).getInputStream()) {
                return JasperCompileManager.compileReport(jrxml);
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo compilar la plantilla " + ruta, e);
            }
        });
    }
}
