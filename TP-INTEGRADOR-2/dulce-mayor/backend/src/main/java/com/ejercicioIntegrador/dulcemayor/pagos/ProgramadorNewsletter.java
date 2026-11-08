package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.service.ServicioNewsletter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tareas programadas del sistema: envío del newsletter el día configurado (5 por defecto) y, en la
 * misma ejecución, respeto de las bajas registradas. La expiración de pagos vive en
 * {@link ProgramadorExpiracionPagos}.
 */
@Component
@ConditionalOnProperty(name = "app.newsletter.habilitado", havingValue = "true", matchIfMissing = true)
public class ProgramadorNewsletter {

    private static final Logger LOG = LoggerFactory.getLogger(ProgramadorNewsletter.class);

    private final ServicioNewsletter servicioNewsletter;
    private final String contenido;

    public ProgramadorNewsletter(ServicioNewsletter servicioNewsletter,
                                 @Value("${app.newsletter.contenido:Novedades del mes de Dulce Mayor.}") String contenido) {
        this.servicioNewsletter = servicioNewsletter;
        this.contenido = contenido;
    }

    /** Por defecto, el día 5 de cada mes a las 9:00 hora local; configurable con NEWSLETTER_CRON. */
    @Scheduled(cron = "${app.newsletter.cron:0 0 9 5 * *}")
    public void enviarEdicionMensual() {
        try {
            int enviados = servicioNewsletter.enviarEdicionMensual(contenido);
            LOG.info("Newsletter del día {} enviado a {} suscriptores", java.time.LocalDate.now(), enviados);
        } catch (RuntimeException e) {
            LOG.warn("No se pudo completar el envío del newsletter", e);
        }
    }
}
