package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.SuscripcionNewsletterResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoCorreo;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.SuscripcionNewsletter;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioNewsletter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Newsletter mensual con opt-in y baja por token, sin necesidad de cuenta. */
@Service
public class ServicioNewsletter {

    private static final Logger LOG = LoggerFactory.getLogger(ServicioNewsletter.class);

    private final RepositorioNewsletter repositorioNewsletter;
    private final ServicioCorreo servicioCorreo;
    private final String urlSitio;

    public ServicioNewsletter(RepositorioNewsletter repositorioNewsletter, ServicioCorreo servicioCorreo,
                              @Value("${app.newsletter.url-sitio:http://localhost:8082}") String urlSitio) {
        this.repositorioNewsletter = repositorioNewsletter;
        this.servicioCorreo = servicioCorreo;
        this.urlSitio = urlSitio;
    }

    @Transactional(rollbackFor = Exception.class)
    public SuscripcionNewsletterResponseDTO suscribir(String email) throws MiException {
        String limpio = normalizar(email);
        if (limpio == null) {
            throw new MiException("Indicá un correo electrónico válido");
        }
        SuscripcionNewsletter suscripcion = repositorioNewsletter
                .findByEmailIgnoreCaseAndEliminadoFalse(limpio).orElseGet(() -> {
                    SuscripcionNewsletter nueva = new SuscripcionNewsletter();
                    nueva.setEmail(limpio);
                    nueva.setActiva(true);
                    return repositorioNewsletter.save(nueva);
                });
        boolean reactivada = !suscripcion.isActiva();
        suscripcion.setActiva(true);
        suscripcion.setFechaBaja(null);
        repositorioNewsletter.save(suscripcion);
        if (reactivada) {
            LOG.info("Newsletter reactivado para {}", limpio);
        }
        servicioCorreo.enviar(limpio, "Te sumaste al newsletter de Dulce Mayor",
                """
                        Hola:

                        Confirmamos tu suscripción al newsletter mensual de Dulce Mayor.

                        Para darte de baja en cualquier momento entrá a:
                        %s/api/newsletter/baja?token=%s

                        Dulce Mayor
                        """.formatted(urlSitio, suscripcion.getTokenBaja()), TipoCorreo.BIENVENIDA_NEWSLETTER);
        return convertir(suscripcion);
    }

    @Transactional(rollbackFor = Exception.class)
    public SuscripcionNewsletterResponseDTO darDeBaja(String token) throws MiException {
        SuscripcionNewsletter suscripcion = repositorioNewsletter.findByTokenBajaAndEliminadoFalse(token)
                .orElseThrow(() -> new MiException("El enlace de baja no es válido"));
        suscripcion.setActiva(false);
        suscripcion.setFechaBaja(LocalDateTime.now());
        return convertir(repositorioNewsletter.save(suscripcion));
    }

    /**
     * Envío mensual: se ejecuta el día configurado y deja fuera a quienes se dieron de baja antes de
     * esa misma ejecución.
     */
    @Transactional(rollbackFor = Exception.class)
    public int enviarEdicionMensual(String contenido) {
        List<SuscripcionNewsletter> activas = repositorioNewsletter.findByActivaTrueAndEliminadoFalse();
        int enviados = 0;
        for (SuscripcionNewsletter suscripcion : activas) {
            servicioCorreo.enviar(suscripcion.getEmail(), "Novedades de Dulce Mayor",
                    contenido == null ? "Novedades del mes de Dulce Mayor." : contenido, TipoCorreo.NEWSLETTER);
            suscripcion.setFechaUltimoEnvio(LocalDateTime.now());
            repositorioNewsletter.save(suscripcion);
            enviados++;
        }
        LOG.info("Newsletter mensual: {} envíos registrados", enviados);
        return enviados;
    }

    @Transactional(readOnly = true)
    public List<SuscripcionNewsletterResponseDTO> listar() {
        return repositorioNewsletter.findByEliminadoFalse().stream().map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return repositorioNewsletter.findByActivaTrueAndEliminadoFalse().size();
    }

    private SuscripcionNewsletterResponseDTO convertir(SuscripcionNewsletter suscripcion) {
        return new SuscripcionNewsletterResponseDTO(suscripcion.getId(), suscripcion.getEmail(),
                suscripcion.isActiva(), suscripcion.getTokenBaja(), suscripcion.getFechaAlta(),
                suscripcion.getFechaBaja(), suscripcion.getFechaUltimoEnvio());
    }

    private static String normalizar(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String limpio = email.trim().toLowerCase(java.util.Locale.ROOT);
        return limpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") ? limpio : null;
    }
}
