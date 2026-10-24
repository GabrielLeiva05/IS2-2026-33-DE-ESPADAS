package com.ejercicioIntegrador.tiendaderopa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/*
 * Job de sistema (no pertenece a ninguna Empresa del negocio): dispara el
 * envío del reporte diario cada 24hs. No contiene lógica de envío de correo
 * -toda esa regla vive en ServicioConfiguracionCorreoEmpresa-, solo conoce
 * el "cuándo" y delega el "cómo" al service.
 */
@Service
public class EnvioProgramado {

    private final ServicioConfiguracionCorreoEmpresa servicio;

    private final String correo;
    private final String clave;
    private final String smtp;
    private final String puerto;
    private final boolean tls;
    private final String destinatarioReporte;

    // Se inyectan automáticamente los valores desde application.properties
    public EnvioProgramado(
            ServicioConfiguracionCorreoEmpresa servicio,
            @Value("${mail.username}") String correo,
            @Value("${mail.password}") String clave,
            @Value("${mail.host}") String smtp,
            @Value("${mail.port}") String puerto,
            @Value("${mail.tls}") boolean tls,
            @Value("${mail.reporte.destinatario:gabrielnleiva2@gmail.com}") String destinatarioReporte) {
        this.servicio = servicio;
        this.correo = correo;
        this.clave = clave;
        this.smtp = smtp;
        this.puerto = puerto;
        this.tls = tls;
        this.destinatarioReporte = destinatarioReporte;
    }

    // Se ejecuta cada 24 horas (en milisegundos: 86400000 ms)
    @Scheduled(fixedRate = 86400000)
    public void enviarReporteDiario() {
        try {
            servicio.enviarCorreoConCredenciales(
                    correo, clave, smtp, puerto, tls,
                    destinatarioReporte,
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