package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoEnvio;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoCorreo;
import com.ejercicioIntegrador.dulcemayor.model.EnvioCorreo;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioEnvioCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Envío de correo transaccional. Si el archivo {@code .env} define {@code MAIL_HOST}, se envía por
 * SMTP real; si no, el envío se registra como {@code SIMULADO} en la tabla {@code envios_correo} y en
 * el log, de modo que las notificaciones se pueden probar sin proveedor externo.
 */
@Service
public class ServicioCorreo {

    private static final Logger LOG = LoggerFactory.getLogger(ServicioCorreo.class);

    private final RepositorioEnvioCorreo repositorioEnvioCorreo;
    private final Optional<JavaMailSender> mailSender;
    private final String remitente;
    private final boolean habilitado;

    public ServicioCorreo(RepositorioEnvioCorreo repositorioEnvioCorreo,
                          Optional<JavaMailSender> mailSender,
                          @Value("${app.correo.remitente:no-reply@dulcemayor.test}") String remitente,
                          @Value("${app.correo.habilitado:true}") boolean habilitado) {
        this.repositorioEnvioCorreo = repositorioEnvioCorreo;
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.habilitado = habilitado;
    }

    /** true cuando hay un servidor SMTP configurado en el .env. */
    public boolean envioRealDisponible() {
        return mailSender.isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public EnvioCorreo enviar(String destinatario, String asunto, String cuerpo, TipoCorreo tipo) {
        EnvioCorreo registro = new EnvioCorreo();
        registro.setDestinatario(destinatario == null ? "" : destinatario.trim());
        registro.setAsunto(asunto);
        registro.setTipo(tipo);
        if (!habilitado) {
            registro.setEstado(EstadoEnvio.SIMULADO);
            registro.setDetalle("El envío de correo está deshabilitado (app.correo.habilitado=false)");
            return repositorioEnvioCorreo.save(registro);
        }
        if (mailSender.isEmpty()) {
            registro.setEstado(EstadoEnvio.SIMULADO);
            registro.setDetalle("Sin MAIL_HOST configurado: el correo no se envió, solo se registró");
            LOG.info("[correo simulado] {} → {}: {}", tipo, registro.getDestinatario(), asunto);
            return repositorioEnvioCorreo.save(registro);
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(registro.getDestinatario());
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.get().send(mensaje);
            registro.setEstado(EstadoEnvio.ENVIADO);
            return repositorioEnvioCorreo.save(registro);
        } catch (MailException e) {
            LOG.warn("No se pudo enviar el correo a {}: {}", registro.getDestinatario(), e.getMessage());
            registro.setEstado(EstadoEnvio.FALLIDO);
            registro.setDetalle(recortar(e.getMessage()));
            return repositorioEnvioCorreo.save(registro);
        }
    }

    /**
     * Envío con cuerpo HTML e imágenes incrustadas (CID). Se usa para el correo del QR de Mercado
     * Pago: la imagen viaja dentro del mensaje, sin enlaces externos. Sin SMTP configurado queda
     * registrado como {@code SIMULADO}.
     */
    @Transactional(rollbackFor = Exception.class)
    public EnvioCorreo enviarHtml(String destinatario, String asunto, String html, TipoCorreo tipo,
                                  Map<String, byte[]> imagenesIncrustadas) {
        EnvioCorreo registro = new EnvioCorreo();
        registro.setDestinatario(destinatario == null ? "" : destinatario.trim());
        registro.setAsunto(asunto);
        registro.setTipo(tipo);
        boolean conImagenes = imagenesIncrustadas != null && !imagenesIncrustadas.isEmpty();
        if (!habilitado) {
            registro.setEstado(EstadoEnvio.SIMULADO);
            registro.setDetalle("El envío de correo está deshabilitado (app.correo.habilitado=false)");
            return repositorioEnvioCorreo.save(registro);
        }
        if (mailSender.isEmpty()) {
            registro.setEstado(EstadoEnvio.SIMULADO);
            registro.setDetalle(conImagenes
                    ? "Sin MAIL_HOST configurado: el correo con QR no se envió, solo se registró"
                    : "Sin MAIL_HOST configurado: el correo no se envió, solo se registró");
            LOG.info("[correo simulado] {} → {}: {} (html{})", tipo, registro.getDestinatario(), asunto,
                    conImagenes ? ", con imágenes incrustadas" : "");
            return repositorioEnvioCorreo.save(registro);
        }
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.get().createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper =
                    new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(registro.getDestinatario());
            helper.setSubject(asunto);
            helper.setText(html, true);
            if (conImagenes) {
                for (Map.Entry<String, byte[]> imagen : imagenesIncrustadas.entrySet()) {
                    helper.addInline(imagen.getKey(),
                            new org.springframework.core.io.ByteArrayResource(imagen.getValue()), "image/png");
                }
            }
            mailSender.get().send(mensaje);
            registro.setEstado(EstadoEnvio.ENVIADO);
            return repositorioEnvioCorreo.save(registro);
        } catch (jakarta.mail.MessagingException | MailException e) {
            LOG.warn("No se pudo enviar el correo a {}: {}", registro.getDestinatario(), e.getMessage());
            registro.setEstado(EstadoEnvio.FALLIDO);
            registro.setDetalle(recortar(e.getMessage()));
            return repositorioEnvioCorreo.save(registro);
        }
    }

    @Transactional(readOnly = true)
    public List<EnvioCorreo> listar() {
        return repositorioEnvioCorreo.findByEliminadoFalseOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public List<EnvioCorreo> listarPorDestinatario(String destinatario) {
        return repositorioEnvioCorreo.findByDestinatarioIgnoreCaseOrderByFechaCreacionDesc(destinatario);
    }

    private static String recortar(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.length() <= 480 ? texto : texto.substring(0, 480);
    }
}
