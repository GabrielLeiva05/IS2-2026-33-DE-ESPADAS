package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Comprobante;
import com.ejercicioIntegrador.dulcemayor.model.LineaPedido;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioComprobante;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Comprobante interno no fiscal: se emite una sola vez, cuando el pago total queda acreditado, y
 * se descarga como HTML imprimible autenticado (sin dependencias nuevas de PDF).
 */
@Service
public class ServicioComprobante {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.of("es", "AR"));
    private static final String AVISO =
            "Comprobante interno no fiscal. No reemplaza a la factura ni tiene validez impositiva.";

    private final RepositorioComprobante repositorioComprobante;

    public ServicioComprobante(RepositorioComprobante repositorioComprobante) {
        this.repositorioComprobante = repositorioComprobante;
    }

    /** Emisión idempotente: si el pedido ya tiene comprobante, devuelve el existente. */
    @Transactional(rollbackFor = Exception.class)
    public Comprobante emitir(Pago pago) {
        return repositorioComprobante.findFirstByPago_IdAndEliminadoFalse(pago.getId()).orElseGet(() -> {
            Comprobante comprobante = new Comprobante();
            comprobante.setPedido(pago.getPedido());
            comprobante.setPago(pago);
            comprobante.setImporte(pago.getImporte().setScale(2, RoundingMode.HALF_UP));
            comprobante.setNumero(numero(pago));
            return repositorioComprobante.save(comprobante);
        });
    }

    @Transactional(readOnly = true)
    public ComprobanteHtml obtenerHtml(String idPedido) throws MiException {
        Comprobante comprobante = repositorioComprobante.findFirstByPedido_IdAndEliminadoFalse(idPedido)
                .orElseThrow(() -> new MiException(
                        "El pedido todavía no tiene comprobante: el pago total no está acreditado"));
        return new ComprobanteHtml("comprobante-" + comprobante.getNumero() + ".html", html(comprobante));
    }

    @Transactional(readOnly = true)
    public boolean existeComprobante(String idPedido) {
        return repositorioComprobante.findFirstByPedido_IdAndEliminadoFalse(idPedido).isPresent();
    }

    private String numero(Pago pago) {
        String idPedido = pago.getPedido().getId() == null ? "0" : pago.getPedido().getId();
        String sufijo = idPedido.replace("-", "");
        return "C-" + sufijo.substring(0, Math.min(10, sufijo.length())).toUpperCase(Locale.ROOT);
    }

    private String html(Comprobante comprobante) {
        Pedido pedido = comprobante.getPedido();
        StringBuilder filas = new StringBuilder();
        for (LineaPedido linea : pedido.getLineas()) {
            filas.append("<tr><td>").append(escapar(linea.getNombreProducto()))
                    .append("</td><td class=\"num\">").append(linea.getCantidadPresentaciones())
                    .append("</td><td class=\"num\">").append(linea.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP))
                    .append("</td><td class=\"num\">").append(linea.getSubtotal().setScale(2, RoundingMode.HALF_UP))
                    .append("</td></tr>");
        }
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <title>Comprobante %s</title>
                  <style>
                    body { font-family: system-ui, -apple-system, "Segoe UI", Arial, sans-serif; margin: 32px; color: #1f2937; }
                    h1 { font-size: 20px; margin: 0 0 4px; }
                    .marca { color: #6d28d9; letter-spacing: .12em; font-size: 12px; text-transform: uppercase; }
                    .meta { margin: 18px 0; font-size: 14px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 16px; font-size: 14px; }
                    th, td { border-bottom: 1px solid #e5e7eb; padding: 8px 6px; text-align: left; }
                    th { background: #f9fafb; font-size: 12px; text-transform: uppercase; letter-spacing: .06em; }
                    .num { text-align: right; white-space: nowrap; }
                    .total { margin-top: 16px; font-size: 18px; text-align: right; }
                    .aviso { margin-top: 28px; padding: 12px; background: #fef3c7; border-radius: 8px; font-size: 12px; }
                  </style>
                </head>
                <body onload="window.print()">
                  <p class="marca">Dulce Mayor</p>
                  <h1>Comprobante interno no fiscal</h1>
                  <p class="meta">
                    <strong>Número:</strong> %s<br>
                    <strong>Emisión:</strong> %s<br>
                    <strong>Pedido:</strong> %s<br>
                    <strong>Comercio:</strong> %s (%s)<br>
                    <strong>Medio de pago:</strong> %s
                  </p>
                  <table>
                    <thead><tr><th>Producto</th><th class="num">Cant.</th><th class="num">Precio</th><th class="num">Subtotal</th></tr></thead>
                    <tbody>%s</tbody>
                  </table>
                  <p class="total"><strong>Total: $ %s</strong></p>
                  <p class="aviso">%s</p>
                </body>
                </html>
                """.formatted(
                escapar(comprobante.getNumero()),
                escapar(comprobante.getNumero()),
                comprobante.getFechaEmision() == null ? "-" : FECHA.format(comprobante.getFechaEmision()),
                escapar(pedido.getId()),
                escapar(pedido.getPuntoVenta().getNombreComercial()),
                escapar(nombreResponsable(pedido)),
                comprobante.getPago().getMedio(),
                filas,
                importe(comprobante.getImporte()),
                AVISO);
    }

    private static String nombreResponsable(Pedido pedido) {
        var cliente = pedido.getPuntoVenta().getCliente();
        return (cliente.getNombre() + " " + cliente.getApellido()).trim();
    }

    private static String importe(BigDecimal valor) {
        return String.format(Locale.of("es", "AR"), "%,.2f", valor);
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /** Documento listo para descargar. */
    public record ComprobanteHtml(String nombreArchivo, String html) {
    }
}
