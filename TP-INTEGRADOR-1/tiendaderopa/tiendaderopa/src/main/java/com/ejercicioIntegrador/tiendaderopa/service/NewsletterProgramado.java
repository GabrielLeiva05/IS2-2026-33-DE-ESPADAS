package com.ejercicioIntegrador.tiendaderopa.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NewsletterProgramado {

    private final ServicioNewsletter servicio;

    public NewsletterProgramado(ServicioNewsletter servicio) {
        this.servicio = servicio;
    }

    // Todos los días a las 9:00. Si la app estuvo apagada, al día siguiente "se pone al día".
    @Scheduled(cron = "${newsletter.cron:0 0 9 * * *}")
    public void ejecutar() {
        servicio.enviarSiCorresponde();
    }
}