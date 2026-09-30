package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.dto.ProductoOfertaDTO;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.EnvioNewsletter;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.VigenciaPrecio;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioEnvioNewsletter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioNewsletter {

    private static final Logger log = LoggerFactory.getLogger(ServicioNewsletter.class);
    private static final String ASUNTO = "Ofertas de Zero para vos";

    @Autowired
    private RepositorioEnvioNewsletter repositorio;          // su repository
    @Autowired private ServicioProducto svcProducto;                    // Service -> Service
    @Autowired private ServicioVigenciaPrecio svcVigenciaPrecio;        // Service -> Service
    @Autowired private UsuarioServicio svcUsuario;                      // Service -> Service
    @Autowired private ServicioConfiguracionCorreoEmpresa svcCorreo;    // Service -> Service
    @Autowired private TemplateEngine templateEngine;                   // Thymeleaf

    // Misma casilla de sistema que ya usa EnvioProgramado (application.properties)
    @Value("${mail.username}") private String correo;
    @Value("${mail.password}") private String clave;
    @Value("${mail.host}") private String smtp;
    @Value("${mail.port}") private String puerto;
    @Value("${mail.tls}") private boolean tls;
    @Value("${newsletter.intervalo-dias:10}") private int intervaloDias;

    /** Lo llama el job diario: envía solo si ya pasó el intervalo. */
    public void enviarSiCorresponde() {
        if (!corresponderEnviar()) {
            log.debug("Newsletter: todavía no pasaron {} días desde el último envío", intervaloDias);
            return;
        }
        try {
            EnvioNewsletter envio = enviarNewsletter();
            log.info("Newsletter enviado a {} clientes ({} fallidos)",
                    envio.getDestinatariosEnviados(), envio.getDestinatariosFallidos());
        } catch (MiException e) {
            log.warn("Newsletter no enviado: {}", e.getMessage()); // se reintenta mañana
        }
    }

    /** Envío forzado (botón "Enviar ahora" del panel). Reinicia el ciclo de 10 días. */
    public EnvioNewsletter enviarNewsletter() throws MiException {
        List<ProductoOfertaDTO> productos = armarProductosEnOferta();
        if (productos.isEmpty()) {
            throw new MiException("No hay productos en oferta con precio vigente para enviar");
        }
        List<String> destinatarios = svcUsuario.listarCorreosClientesActivos();
        if (destinatarios.isEmpty()) {
            throw new MiException("No hay clientes activos a quienes enviar el newsletter");
        }

        String html = renderizar(productos);
        int enviados = 0;
        int fallidos = 0;
        for (String destinatario : destinatarios) {
            try {
                svcCorreo.enviarCorreoConCredenciales(correo, clave, smtp, puerto, tls,
                        destinatario, ASUNTO, html);
                enviados++;
            } catch (MiException e) {
                fallidos++;
                log.warn("No se pudo enviar el newsletter a {}: {}", destinatario, e.getMessage());
            }
        }
        if (enviados == 0) {
            // No se registra: así el job vuelve a intentar mañana
            throw new MiException("No se pudo enviar a ningún cliente. Revisá la configuración de correo");
        }
        return repositorio.save(new EnvioNewsletter(LocalDateTime.now(), productos.size(), enviados, fallidos));
    }

    /** Para la vista previa del panel (renderiza sin enviar). */
    public String generarHtml() {
        return renderizar(armarProductosEnOferta());
    }

    public List<EnvioNewsletter> listarEnvios() {
        return repositorio.findAllByOrderByFechaEnvioDesc();
    }

    // ---------- privados ----------

    private boolean corresponderEnviar() {
        // Se comparan FECHAS (no horas) para que el envío no se corra un día por segundos de diferencia
        return repositorio.findTopByOrderByFechaEnvioDesc()
                .map(ultimo -> ChronoUnit.DAYS.between(ultimo.getFechaEnvio().toLocalDate(), LocalDate.now()) >= intervaloDias)
                .orElse(true);
    }

    private List<ProductoOfertaDTO> armarProductosEnOferta() {
        List<ProductoOfertaDTO> lista = new ArrayList<>();
        for (Producto p : svcProducto.listarProductoEnOferta()) {
            VigenciaPrecio vigente = svcVigenciaPrecio.buscarVigenciaPrecioVigente(p.getId());
            if (vigente == null) continue; // sin precio vigente no se puede publicitar

            String categoria = "";
            if (p.getSubCategoria() != null) {
                String cat = p.getSubCategoria().getCategoria() != null
                        ? p.getSubCategoria().getCategoria().getNombre() : "";
                categoria = cat.isEmpty() ? p.getSubCategoria().getNombre()
                        : cat + " · " + p.getSubCategoria().getNombre();
            }
            lista.add(new ProductoOfertaDTO(p.getNombre(), p.getDescripcion(), p.getTalle(),
                    categoria, vigente.getPrecio()));
        }
        return lista;
    }

    private String renderizar(List<ProductoOfertaDTO> productos) {
        Context ctx = new Context();
        ctx.setVariable("productos", productos);
        ctx.setVariable("fecha", LocalDate.now());
        return templateEngine.process("email/newsletter", ctx); // templates/email/newsletter.html
    }
}