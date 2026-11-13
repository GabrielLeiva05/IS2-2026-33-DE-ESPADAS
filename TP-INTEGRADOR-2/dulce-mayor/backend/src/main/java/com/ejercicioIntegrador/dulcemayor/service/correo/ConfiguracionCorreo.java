package com.ejercicioIntegrador.dulcemayor.service.correo;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Arma la cadena de decoradores del correo (la nota "a = new ...; b = new ...(a)" del diagrama del patrón):
 *
 * <pre>
 *   Habilitado → Tolerante → Reintentos → SMTP
 * </pre>
 *
 * El orden importa:
 *  1. Habilitado va afuera: si el correo está apagado no tiene sentido hacer nada más.
 *  2. Tolerante envuelve a los reintentos: solo cuando se agotaron todos los intentos se absorbe la falla.
 *  3. Reintentos envuelve directamente al envío real por SMTP.
 * Para quitar o sumar una capa (por ejemplo, redirigir todo a una casilla de prueba) solo se cambia este método.
 */
@Configuration
public class ConfiguracionCorreo {

    @Bean
    public ComponenteCorreo componenteCorreo(ObjectProvider<JavaMailSender> proveedorMailSender,
                                             @Value("${app.correo.habilitado:false}") boolean habilitado,
                                             @Value("${app.correo.remitente:no-responder@dulcemayor.com.ar}") String remitente,
                                             @Value("${app.correo.nombre-remitente:Dulce Mayor}") String nombreRemitente,
                                             @Value("${app.correo.intentos:3}") int intentos,
                                             @Value("${app.correo.pausa-reintento-ms:1000}") long pausaMilisegundos) {
        ComponenteCorreo smtp = new ComponenteConcretoCorreoSmtp(proveedorMailSender, remitente, nombreRemitente);
        ComponenteCorreo conReintentos = new DecoradorConcretoCorreoReintentos(smtp, intentos, pausaMilisegundos);
        ComponenteCorreo tolerante = new DecoradorConcretoCorreoTolerante(conReintentos);
        return new DecoradorConcretoCorreoHabilitado(tolerante, habilitado);
    }
}
