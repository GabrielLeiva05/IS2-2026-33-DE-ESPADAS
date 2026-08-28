package com.is2.tinder.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

	private final JavaMailSender mailSender;

	public NotificacionService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
	public void enviar(String cuerpo, String titulo, String mail) {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setTo(mail);
		mensaje.setFrom("noreply@tinder-mascota.com");
		mensaje.setSubject(titulo);
		mensaje.setText(cuerpo);

		mailSender.send(mensaje);
	}
}
