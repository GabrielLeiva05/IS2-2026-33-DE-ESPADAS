package ar.com.dulcemayor.osm.service;

import ar.com.dulcemayor.osm.model.PuntoVenta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final ObjectProvider<JavaMailSender> mail;
    private final String from;

    public NotificacionService(ObjectProvider<JavaMailSender> mail, @Value("${app.mail.from}") String from) {
        this.mail = mail;
        this.from = from;
    }

    /** Avisa al proximo punto de la ruta que el despacho llegara en breve. */
    public String avisarProximaEntrega(PuntoVenta p) {
        String asunto = "Dulce Mayor: su pedido llega en breve";
        String cuerpo = "Hola " + p.getNombre() + ",\n\nNuestro equipo de despacho se dirige a "
                + p.getDireccion() + " y llegara en breve con su pedido.\n\nDulce Mayor";
        JavaMailSender sender = mail.getIfAvailable();
        if (sender == null || p.getEmail() == null || p.getEmail().isBlank()) {
            log.info("[EMAIL SIMULADO] a={} asunto='{}'", p.getEmail(), asunto);
            return "Email simulado a " + p.getEmail();
        }
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(from); m.setTo(p.getEmail()); m.setSubject(asunto); m.setText(cuerpo);
            sender.send(m);
            return "Email enviado a " + p.getEmail();
        } catch (Exception e) {
            log.warn("No se pudo enviar el email: {}", e.getMessage());
            return "Fallo el envio de email a " + p.getEmail();
        }
    }
}
