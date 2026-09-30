package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ConfiguracionCorreoEmpresa;
import com.ejercicioIntegrador.tiendaderopa.model.Empresa;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioConfiguracionCorreoEmpresa;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioEmpresa;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/*
 * Todas las reglas de negocio de ConfiguracionCorreoEmpresa viven acá,
 * incluyendo el envío de correo.
 */
@Service
public class ServicioConfiguracionCorreoEmpresa {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    @Autowired
    private RepositorioConfiguracionCorreoEmpresa repositorio;

    @Autowired
    private RepositorioEmpresa empresaRepositorio;

    @Transactional
    public ConfiguracionCorreoEmpresa crearConfiguracionCorreoAutomatico(String correo, String clave, String puerto,
                                                                          String smtp, boolean tls, String idEmpresa) throws MiException {

        validar(correo, clave, puerto, smtp, tls, idEmpresa);

        Empresa empresa = empresaRepositorio.findById(idEmpresa)
                .orElseThrow(() -> new MiException("No existe una empresa con id: " + idEmpresa));

        ConfiguracionCorreoEmpresa configuracion =
                new ConfiguracionCorreoEmpresa(correo.trim(), clave, puerto.trim(), smtp.trim(), tls, empresa);

        return this.repositorio.save(configuracion);
    }

    public void validar(String correo, String clave, String puerto, String smtp, boolean tls, String idEmpresa) throws MiException {
        if (correo == null || correo.trim().isEmpty()) {
            throw new MiException("El correo no puede estar vacío");
        }
        if (!EMAIL_PATTERN.matcher(correo.trim()).matches()) {
            throw new MiException("El correo ingresado no es válido");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new MiException("La clave no puede estar vacía");
        }
        if (puerto == null || puerto.trim().isEmpty() || !puerto.trim().matches("\\d{1,5}")
                || Integer.parseInt(puerto.trim()) < 1 || Integer.parseInt(puerto.trim()) > 65535) {
            throw new MiException("El puerto debe ser un número válido entre 1 y 65535");
        }
        if (smtp == null || smtp.trim().isEmpty()) {
            throw new MiException("El servidor SMTP no puede estar vacío");
        }
        if (idEmpresa == null || idEmpresa.trim().isEmpty()) {
            throw new MiException("Debe indicar la empresa a la que pertenece la configuración");
        }
    }

    @Transactional
    public ConfiguracionCorreoEmpresa buscarConfiguracionCorreoAutomatico(String id) throws MiException {
        return this.repositorio.findById(id)
                .orElseThrow(() -> new MiException("No existe una configuración de correo con id: " + id));
    }

    @Transactional
    public ConfiguracionCorreoEmpresa modificarConfiguracionCorreoAutomatico(String id, String correo, String clave,
                                                                              String puerto, String smtp, boolean tls,
                                                                              String idEmpresa) throws MiException {

        validar(correo, clave, puerto, smtp, tls, idEmpresa);

        ConfiguracionCorreoEmpresa configuracion = buscarConfiguracionCorreoAutomatico(id);

        Empresa empresa = empresaRepositorio.findById(idEmpresa)
                .orElseThrow(() -> new MiException("No existe una empresa con id: " + idEmpresa));

        configuracion.setCorreo(correo.trim());
        configuracion.setClave(clave);
        configuracion.setPuerto(puerto.trim());
        configuracion.setSmtp(smtp.trim());
        configuracion.setTls(tls);
        configuracion.setEmpresa(empresa);

        return this.repositorio.save(configuracion);
    }

    @Transactional
    public void eliminarConfiguracionCorreoAutomatico(String id) throws MiException {
        ConfiguracionCorreoEmpresa configuracion = buscarConfiguracionCorreoAutomatico(id);
        // baja lógica
        configuracion.setEliminado(true);
        this.repositorio.save(configuracion);
    }

    @Transactional
    public List<ConfiguracionCorreoEmpresa> listarConfiguracionCorreoAutomatico() {
        return this.repositorio.findAll();
    }

    @Transactional
    public List<ConfiguracionCorreoEmpresa> listarConfiguracionCorreoAutomaticoActiva() {
        return this.repositorio.findByEliminadoFalse();
    }

    /**
     * Envía un correo usando los datos de una ConfiguracionCorreoEmpresa ya
     * persistida (es decir, la casilla configurada para una Empresa puntual).
     * Es la única lógica de negocio "activa" de esta clase, y por eso vive
     * en el service y no en la entidad.
     */
    @Transactional
    public void enviarCorreo(String idConfiguracion, String destinatario, String asunto, String cuerpoHtml) throws MiException {
        ConfiguracionCorreoEmpresa configuracion = buscarConfiguracionCorreoAutomatico(idConfiguracion);

        enviarCorreoInterno(configuracion.getCorreo(), configuracion.getClave(), configuracion.getSmtp(),
                configuracion.getPuerto(), configuracion.isTls(), destinatario, asunto, cuerpoHtml);
    }

    /**
     * Variante para envíos que NO están atados a ninguna Empresa del negocio
     * (por ejemplo, un mailer de sistema/reportes que usa una casilla propia
     * de la aplicación, configurada por application.properties). No requiere
     * que exista una ConfiguracionCorreoEmpresa persistida.
     *
     * Reutiliza el mismo motor de envío que enviarCorreo(idConfiguracion,...),
     * así toda la lógica de "cómo se manda un mail" queda en un solo lugar.
     */
    public void enviarCorreoConCredenciales(String correo, String clave, String smtp, String puerto, boolean tls,
                                             String destinatario, String asunto, String cuerpoHtml) throws MiException {

        if (correo == null || correo.trim().isEmpty() || !EMAIL_PATTERN.matcher(correo.trim()).matches()) {
            throw new MiException("La casilla remitente configurada no es válida");
        }
        if (smtp == null || smtp.trim().isEmpty() || puerto == null || puerto.trim().isEmpty()) {
            throw new MiException("Falta configurar el servidor SMTP o el puerto");
        }

        enviarCorreoInterno(correo, clave, smtp, puerto, tls, destinatario, asunto, cuerpoHtml);
    }

    private void enviarCorreoInterno(String correo, String clave, String smtp, String puerto, boolean tls,
                                      String destinatario, String asunto, String cuerpoHtml) throws MiException {

        if (destinatario == null || destinatario.trim().isEmpty()) {
            throw new MiException("Debe indicar un destinatario");
        }
        if (asunto == null || asunto.trim().isEmpty()) {
            throw new MiException("Debe indicar un asunto");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));
        props.put("mail.smtp.host", smtp);
        props.put("mail.smtp.port", puerto);

        final String correoFinal = correo;
        final String claveFinal = clave;

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correoFinal, claveFinal);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(correo));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setContent(cuerpoHtml, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            throw new MiException("No se pudo enviar el correo: " + e.getMessage());
        }
    }
}