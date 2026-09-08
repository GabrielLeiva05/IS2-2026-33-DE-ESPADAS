package com.ejercicioIntegrador.tiendaderopa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EnvioProgramado {

    private final ConfiguracionCorreoEmpresa cce;

    // Se inyectan automáticamente los valores desde application.properties
    public EnvioProgramado(
            @Value("${mail.username}") String username,
            @Value("${mail.host}") String host,
            @Value("${mail.tls}") boolean tls,
            @Value("${mail.port}") String port,
            @Value("${mail.password}") String password) {
        this.cce = new ConfiguracionCorreoEmpresa(username, host, tls, port, password);
    }

    // Se ejecuta cada 24 horas (en milisegundos: 86400000 ms)
    @Scheduled(fixedRate = 86400000)
    public void enviarReporteDiario() {
        try {
            cce.enviarCorreo(
                "JuanPenaranda050@gmail.com", 
                "Reporte Diario", 
                "<h1>Reporte listo</h1>"
            );
            System.out.println("Correo automático enviado con éxito.");
        } catch (Exception e) {
            System.err.println("Error al enviar el correo automático:");
            e.printStackTrace();
        }
    }
}