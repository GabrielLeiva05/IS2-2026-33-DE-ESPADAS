package com.is2.tinder.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public NotificacionService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Async
    public void enviar(String cuerpo, String titulo, String mail) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            logger.info("SMTP no configurado. No se envía correo a {} con asunto '{}'. Cuerpo: {}", mail, titulo, cuerpo);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(mail);
            mensaje.setFrom("noreply@tinder-mascota.com");
            mensaje.setSubject(titulo);
            mensaje.setText(cuerpo);

            mailSender.send(mensaje);
        } catch (Exception ex) {
            logger.warn("No se pudo enviar el correo de notificación a {}. Se ignora porque la app está en modo desarrollo. Detalle: {}",
                    mail, ex.getMessage());
        }
    }
}
