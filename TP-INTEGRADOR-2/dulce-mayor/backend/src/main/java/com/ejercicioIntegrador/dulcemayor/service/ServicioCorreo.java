package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.FormaDePago;
import com.ejercicioIntegrador.dulcemayor.model.DetallePedido;
import com.ejercicioIntegrador.dulcemayor.model.FacturaCliente;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Envío de correos al cliente cuando el preventista confirma un pedido en forma presencial.
 *
 * Contenido por medio de pago:
 *  - EFECTIVO: detalle, total y el aviso de que se paga en efectivo al recibir el pedido.
 *  - TRANSFERENCIA: detalle, total y el alias (con titular) para transferir.
 *  - QR: detalle, total y el QR de Mercado Pago adjunto como imagen.
 * Además, cuando Mercado Pago acredita el pago del QR, se envía un correo de confirmación.
 * Y cuando el distribuidor entrega en un comercio, se avisa al comercio que sigue en la ruta que será el
 * próximo visitado.
 *
 * Si el correo no está configurado (app.correo.habilitado=false o sin spring.mail.host) no falla la
 * confirmación del pedido: se registra en el log y se continúa.
 */
@Service
public class ServicioCorreo {

    private static final Logger log = LoggerFactory.getLogger(ServicioCorreo.class);
    private static final Locale ARGENTINA = Locale.of("es", "AR");

    private final ObjectProvider<JavaMailSender> proveedorMailSender;

    @Value("${app.correo.habilitado:false}")
    private boolean habilitado;

    @Value("${app.correo.remitente:no-responder@dulcemayor.com.ar}")
    private String remitente;

    @Value("${app.correo.nombre-remitente:Dulce Mayor}")
    private String nombreRemitente;

    @Value("${app.pagos.titular-transferencia:Dulce Mayor S.A.}")
    private String titularTransferencia;

    public ServicioCorreo(ObjectProvider<JavaMailSender> proveedorMailSender) {
        this.proveedorMailSender = proveedorMailSender;
    }

    /** Correo inicial con el detalle del pedido y las instrucciones de pago según el medio elegido. */
    public void enviarConfirmacionPedido(Pedido pedido, FacturaCliente factura, FormaDePago forma,
                                         String alias, byte[] qrPng) {
        String asunto = "Pedido N.º " + pedido.getNumeroPedido() + " · Dulce Mayor";
        String cuerpo = cuerpoConfirmacion(pedido, factura, forma, alias, qrPng != null);
        enviar(pedido, asunto, cuerpo, qrPng);
    }

    /** Correo final: Mercado Pago acreditó el pago del QR. */
    public void enviarPagoAprobado(Pedido pedido, FacturaCliente factura) {
        String asunto = "Pago acreditado · Pedido N.º " + pedido.getNumeroPedido();
        enviar(pedido, asunto, cuerpoPagoAprobado(pedido, factura), null);
    }

    /**
     * Aviso al comercio que el distribuidor visitará a continuación, apenas entrega en la parada anterior.
     * Es solo informativo: no requiere que el comercio tenga un pedido cargado.
     */
    public void enviarAvisoProximaVisita(PuntoVenta puntoVenta, String nombreDistribuidor) {
        String asunto = "Tu pedido está por llegar · Dulce Mayor";
        enviarA(correoDe(puntoVenta), asunto, cuerpoProximaVisita(puntoVenta, nombreDistribuidor), null);
    }

    // ------------------------------------------------------------------
    // Envío
    // ------------------------------------------------------------------

    private void enviar(Pedido pedido, String asunto, String html, byte[] qrPng) {
        enviarA(correoCliente(pedido), asunto, html, qrPng);
    }

    private void enviarA(String destino, String asunto, String html, byte[] qrPng) {
        if (!habilitado) {
            log.info("Correo deshabilitado: no se envía «{}» a {} (activar CORREO_HABILITADO y MAIL_*)",
                    asunto, destino);
            return;
        }
        JavaMailSender sender = proveedorMailSender.getIfAvailable();
        if (sender == null || destino == null) {
            log.warn("No hay servidor de correo configurado o el cliente no tiene correo: «{}»", asunto);
            return;
        }
        try {
            MimeMessage mensaje = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente, nombreRemitente);
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(html, true);
            if (qrPng != null) {
                helper.addInline("qrPago", new ByteArrayResource(qrPng), "image/png");
            }
            sender.send(mensaje);
        } catch (Exception e) {
            // Un correo fallido no debe tirar abajo la operación que lo dispara (pedido o entrega).
            log.warn("No se pudo enviar «{}» a {}: {}", asunto, destino, e.getMessage());
        }
    }

    private static String correoCliente(Pedido pedido) {
        return correoDe(pedido.getPuntoVenta());
    }

    private static String correoDe(PuntoVenta puntoVenta) {
        Usuario usuario = puntoVenta.getCliente().getUsuario();
        return usuario == null ? null : usuario.getNombreUsuario();
    }

    // ------------------------------------------------------------------
    // Contenido HTML
    // ------------------------------------------------------------------

    private String cuerpoConfirmacion(Pedido pedido, FacturaCliente factura, FormaDePago forma,
                                      String alias, boolean conQr) {
        StringBuilder html = new StringBuilder();
        html.append(encabezadoHtml("Pedido confirmado"));
        html.append("<p>Hola ").append(escapar(nombreCliente(pedido)))
                .append(", confirmamos tu pedido <strong>N.º ").append(pedido.getNumeroPedido())
                .append("</strong> en <strong>").append(escapar(pedido.getPuntoVenta().getNombreFantasia()))
                .append("</strong>.</p>");
        html.append(tablaDetalle(pedido, factura));
        html.append(instruccionesPago(forma, factura.getTotal(), alias, conQr));
        html.append(pieHtml());
        return html.toString();
    }

    private String cuerpoPagoAprobado(Pedido pedido, FacturaCliente factura) {
        StringBuilder html = new StringBuilder();
        html.append(encabezadoHtml("Pago acreditado"));
        html.append("<p>¡Buenas noticias! Mercado Pago acreditó el pago de tu pedido <strong>N.º ")
                .append(pedido.getNumeroPedido()).append("</strong>.</p>");
        html.append("<p>Monto abonado: <strong>").append(pesos(factura.getTotal())).append("</strong>.</p>");
        html.append("<p>Ya estamos preparando el despacho. Te avisaremos cuando salga a reparto.</p>");
        html.append(pieHtml());
        return html.toString();
    }

    private String cuerpoProximaVisita(PuntoVenta puntoVenta, String nombreDistribuidor) {
        String nombre = nombreResponsable(puntoVenta);
        StringBuilder html = new StringBuilder();
        html.append(encabezadoHtml("Tu pedido está por llegar"));
        html.append("<p>").append(nombre.isEmpty() ? "Hola" : "Hola " + escapar(nombre)).append(",</p>");
        html.append("<p>En los próximos minutos estaremos visitando <strong>")
                .append(escapar(puntoVenta.getNombreFantasia()))
                .append("</strong> para entregar tu pedido de Dulce Mayor. ¡Estate atento!</p>");
        if (nombreDistribuidor != null && !nombreDistribuidor.isBlank()) {
            html.append("<p>Te va a atender <strong>").append(escapar(nombreDistribuidor)).append("</strong>.</p>");
        }
        html.append(pieHtml());
        return html.toString();
    }

    private String instruccionesPago(FormaDePago forma, double total, String alias, boolean conQr) {
        if (forma == null) {
            return "";
        }
        double monto = total;
        return switch (forma) {
            case EFECTIVO -> "<div style=\"background:#f4f6f8;padding:16px;border-radius:8px\">"
                    + "<h3 style=\"margin-top:0\">Pago en efectivo</h3>"
                    + "<p>El pago se realizará <strong>en efectivo al recibir el pedido</strong>.</p>"
                    + "<p>Monto a abonar: <strong style=\"font-size:18px\">" + pesos(monto) + "</strong></p></div>";
            case TRANSFERENCIA -> "<div style=\"background:#f4f6f8;padding:16px;border-radius:8px\">"
                    + "<h3 style=\"margin-top:0\">Pago por transferencia</h3>"
                    + "<p>Transferí <strong style=\"font-size:18px\">" + pesos(monto) + "</strong> a:</p>"
                    + "<p>Alias: <strong>" + escapar(alias) + "</strong><br>Titular: "
                    + escapar(titularTransferencia) + "</p>"
                    + "<p>Cuando la realices, respondé este correo con el comprobante.</p></div>";
            case QR -> "<div style=\"background:#f4f6f8;padding:16px;border-radius:8px\">"
                    + "<h3 style=\"margin-top:0\">Pago con QR de Mercado Pago</h3>"
                    + "<p>Escaneá el código QR adjunto con la app de Mercado Pago para pagar "
                    + "<strong style=\"font-size:18px\">" + pesos(monto) + "</strong>.</p>"
                    + (conQr ? "<p style=\"text-align:center\"><img src=\"cid:qrPago\" alt=\"QR de pago\" "
                    + "style=\"width:220px;height:220px\"></p>" : "")
                    + "<p>Cuando el pago se acredite, vas a recibir otro correo de confirmación.</p></div>";
        };
    }

    private String tablaDetalle(Pedido pedido, FacturaCliente factura) {
        StringBuilder html = new StringBuilder();
        html.append("<table style=\"width:100%;border-collapse:collapse;margin:16px 0\">")
                .append("<thead><tr style=\"text-align:left;border-bottom:1px solid #ddd\">")
                .append("<th style=\"padding:6px\">Producto</th>")
                .append("<th style=\"padding:6px\">Cant.</th>")
                .append("<th style=\"padding:6px;text-align:right\">Subtotal</th></tr></thead><tbody>");
        for (DetallePedido detalle : pedido.getDetalles()) {
            html.append("<tr style=\"border-bottom:1px solid #eee\">")
                    .append("<td style=\"padding:6px\">").append(escapar(detalle.getProducto().getNombre()))
                    .append("</td>")
                    .append("<td style=\"padding:6px\">").append(detalle.getCantidad()).append("</td>")
                    .append("<td style=\"padding:6px;text-align:right\">").append(pesos(detalle.getSubtotal()))
                    .append("</td></tr>");
        }
        html.append("</tbody></table>");
        html.append("<table style=\"width:100%;margin-bottom:16px\">")
                .append("<tr><td>Subtotal</td><td style=\"text-align:right\">").append(pesos(factura.getSubtotal()))
                .append("</td></tr>")
                .append("<tr><td>IVA</td><td style=\"text-align:right\">").append(pesos(factura.getIva()))
                .append("</td></tr>")
                .append("<tr><td style=\"font-weight:bold;font-size:16px\">Total</td>")
                .append("<td style=\"text-align:right;font-weight:bold;font-size:16px\">")
                .append(pesos(factura.getTotal())).append("</td></tr></table>");
        return html.toString();
    }

    private static String encabezadoHtml(String titulo) {
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto\">"
                + "<h2 style=\"color:#6d28d9\">Dulce Mayor</h2>"
                + "<h3>" + titulo + "</h3>";
    }

    private static String pieHtml() {
        return "<p style=\"color:#888;font-size:12px;margin-top:24px\">Dulce Mayor · Mayorista de golosinas · "
                + "Mendoza, Argentina. Este correo se envió automáticamente.</p></div>";
    }

    private static String nombreCliente(Pedido pedido) {
        return nombreResponsable(pedido.getPuntoVenta());
    }

    private static String nombreResponsable(PuntoVenta puntoVenta) {
        var cliente = puntoVenta.getCliente();
        if (cliente == null) {
            return "";
        }
        return (cliente.getNombre() + " " + cliente.getApellido()).trim();
    }

    private static String pesos(double valor) {
        NumberFormat formato = NumberFormat.getInstance(ARGENTINA);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return "$ " + formato.format(valor);
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
