package com.icodeap.reportes.reportes.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@Service 
public class ReportService {

    private final DataSource dataSource;

    public ReportService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public byte[] generarReport(String reportName) throws Exception {

    InputStream reportStream = getClass()
            .getResourceAsStream("/reports/" + reportName + ".jasper");

    if (reportStream == null) {
        throw new IllegalArgumentException(
                "No se encontró el reporte: /reports/"
                + reportName + ".jasper"
        );
    }

    Map<String, Object> params = new HashMap<>();

    JasperPrint jasperPrint = JasperFillManager.fillReport(
            reportStream,
            params,
            dataSource.getConnection()
    );

    return JasperExportManager.exportReportToPdf(jasperPrint);
}
}
