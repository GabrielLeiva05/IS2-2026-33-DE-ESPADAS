package com.ejercicioIntegrador.dulcemayor.service.correo;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * PATRÓN DECORATOR — COMPONENTE CONCRETO.
 *
 * El comportamiento básico: arma el MimeMessage y lo entrega al servidor SMTP configurado (spring.mail.*).
 * No decide si corresponde enviar, no reintenta ni absorbe errores: eso lo agregan los decoradores.
 */
public class ComponenteConcretoCorreoSmtp implements ComponenteCorreo {

    private final ObjectProvider<JavaMailSender> proveedorMailSender;
    private final String remitente;
    private final String nombreRemitente;

    public ComponenteConcretoCorreoSmtp(ObjectProvider<JavaMailSender> proveedorMailSender, String remitente,
                                        String nombreRemitente) {
        this.proveedorMailSender = proveedorMailSender;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
    }

    @Override
    public void enviar(MensajeCorreo mensaje) {
        if (mensaje.destino() == null || mensaje.destino().isBlank()) {
            throw new ErrorEnvioCorreoException("El destinatario no tiene correo", false, null);
        }
        JavaMailSender sender = proveedorMailSender.getIfAvailable();
        if (sender == null) {
            throw new ErrorEnvioCorreoException("No hay servidor de correo configurado (MAIL_HOST)", false, null);
        }
        MimeMessage mime;
        try {
            mime = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(remitente, nombreRemitente);
            helper.setTo(mensaje.destino());
            helper.setSubject(mensaje.asunto());
            helper.setText(mensaje.html(), true);
            if (mensaje.imagenQrPng() != null) {
                helper.addInline(MensajeCorreo.ID_IMAGEN_QR, new ByteArrayResource(mensaje.imagenQrPng()), "image/png");
            }
        } catch (Exception e) {
            // Datos inválidos (por ejemplo, una dirección mal formada): reintentar no lo arregla.
            throw new ErrorEnvioCorreoException("No se pudo armar el correo: " + e.getMessage(), false, e);
        }
        try {
            sender.send(mime);
        } catch (MailException e) {
            // El servidor no respondió o rechazó la conexión: puede ser transitorio.
            throw new ErrorEnvioCorreoException("El servidor SMTP no aceptó el correo: " + e.getMessage(), true, e);
        }
    }
}
