package com.ejercicioIntegrador.dulcemayor.service.correo;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Componente concreto del Decorator: arma el MimeMessage y lo entrega; clasifica las fallas. */
class ComponenteConcretoCorreoSmtpTest {

    private static final MensajeCorreo MENSAJE =
            new MensajeCorreo("ana@kiosco.com", "Pedido N.º 1002", "<p>Hola</p>", new byte[]{1, 2, 3});

    @SuppressWarnings("unchecked")
    private static ObjectProvider<JavaMailSender> proveedor(JavaMailSender sender) {
        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(sender);
        return proveedor;
    }

    private static JavaMailSender senderConMensajeReal() {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        return sender;
    }

    @Test
    void armaElMensajeYLoEnvia() throws Exception {
        JavaMailSender sender = senderConMensajeReal();

        new ComponenteConcretoCorreoSmtp(proveedor(sender), "ventas@dulcemayor.com.ar", "Dulce Mayor").enviar(MENSAJE);

        verify(sender).send(any(MimeMessage.class));
    }

    @Test
    void sinServidorConfiguradoFallaSinReintentar() {
        ErrorEnvioCorreoException error = assertThrows(ErrorEnvioCorreoException.class,
                () -> new ComponenteConcretoCorreoSmtp(proveedor(null), "a@b.com", "DM").enviar(MENSAJE));
        assertFalse(error.isReintentable());
    }

    @Test
    void sinDestinatarioNoIntentaEnviar() {
        JavaMailSender sender = senderConMensajeReal();
        MensajeCorreo sinDestino = new MensajeCorreo(null, "Asunto", "<p>x</p>", null);

        ErrorEnvioCorreoException error = assertThrows(ErrorEnvioCorreoException.class,
                () -> new ComponenteConcretoCorreoSmtp(proveedor(sender), "a@b.com", "DM").enviar(sinDestino));
        assertFalse(error.isReintentable());
        verify(sender, never()).send(any(MimeMessage.class));
    }

    @Test
    void siElServidorNoAceptaElCorreoLaFallaEsReintentable() {
        JavaMailSender sender = senderConMensajeReal();
        doThrow(new MailSendException("Connection timed out")).when(sender).send(any(MimeMessage.class));

        ErrorEnvioCorreoException error = assertThrows(ErrorEnvioCorreoException.class,
                () -> new ComponenteConcretoCorreoSmtp(proveedor(sender), "a@b.com", "DM").enviar(MENSAJE));
        assertTrue(error.isReintentable());
        assertEquals(MailSendException.class, error.getCause().getClass());
    }
}
