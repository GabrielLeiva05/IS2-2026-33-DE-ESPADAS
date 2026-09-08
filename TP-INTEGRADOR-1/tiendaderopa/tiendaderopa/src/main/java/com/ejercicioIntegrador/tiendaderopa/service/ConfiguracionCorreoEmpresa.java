package com.ejercicioIntegrador.tiendaderopa.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;


public class ConfiguracionCorreoEmpresa {
    private String id;
    private String correo;
    private String clave;
    private String smtp;
    private boolean tls;
    private boolean eliminado;
    private String puerto;

    public ConfiguracionCorreoEmpresa(String correo, String smtp, boolean tls, String puerto, String clave) {
        this.correo = correo;
        this.smtp = smtp;
        this.tls = tls;
        this.puerto = puerto;
        this.eliminado = false;
        this.clave = clave;
    }
        
    public void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) throws MessagingException{
        // Configuración de propiedades del servidor SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(this.tls));
        props.put("mail.smtp.host", this.smtp);
        props.put("mail.smtp.port", this.puerto);
        
        // Creación de la sesión autenticada
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correo, clave);
            }
        });
        
        // Construcción del mensaje
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(this.correo));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(asunto);
        message.setContent(cuerpoHtml, "text/html; charset=utf-8");

        // Envío a través del protocolo SMTP
        Transport.send(message);
    
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }
    
    
    
    
}