package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Adaptador HTTP de Mercado Pago (Checkout Pro). Sin access token configurado no simula nada:
 * informa que la integración no está configurada. El webhook se valida con la firma HMAC-SHA256
 * que envía el proveedor y luego se consulta el pago server-to-server.
 */
@Component
public class ClienteMercadoPago {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteMercadoPago.class);

    private final HttpClient http;
    private final JsonMapper json = JsonMapper.builder().build();
    private final String urlBase;
    private final String accessToken;
    private final String secretoWebhook;
    private final String urlNotificacion;
    private final String rutaPreferencias;
    private final Duration timeout;

    public ClienteMercadoPago(
            @Value("${app.pagos.mercadopago.base-url:https://api.mercadopago.com}") String urlBase,
            @Value("${app.pagos.mercadopago.access-token:}") String accessToken,
            @Value("${app.pagos.mercadopago.webhook-secret:}") String secretoWebhook,
            @Value("${app.pagos.mercadopago.notificacion-url:}") String urlNotificacion,
            @Value("${app.pagos.mercadopago.ruta-preferencias:/preferences}") String rutaPreferencias,
            @Value("${app.pagos.mercadopago.timeout-ms:8000}") long timeoutMs) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.accessToken = accessToken == null ? "" : accessToken.trim();
        this.secretoWebhook = secretoWebhook == null ? "" : secretoWebhook.trim();
        this.urlNotificacion = urlNotificacion == null ? "" : urlNotificacion.trim();
        this.rutaPreferencias = rutaPreferencias.startsWith("/") ? rutaPreferencias : "/" + rutaPreferencias;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.http = HttpClient.newBuilder().connectTimeout(this.timeout).build();
    }

    public boolean estaConfigurado() {
        return !accessToken.isEmpty();
    }

    public boolean webhookConfigurado() {
        return !secretoWebhook.isEmpty();
    }

    public Preferencia crearPreferencia(String referenciaOperacion, BigDecimal importe, String descripcion)
            throws MiException {
        exigirConfiguracion();
        Map<String, Object> item = new HashMap<>();
        item.put("id", referenciaOperacion);
        item.put("title", descripcion);
        item.put("quantity", 1);
        item.put("currency_id", "ARS");
        item.put("unit_price", importe.doubleValue());

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("items", List.of(item));
        cuerpo.put("external_reference", referenciaOperacion);
        if (!urlNotificacion.isEmpty()) {
            cuerpo.put("notification_url", urlNotificacion);
        }

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(urlBase + rutaPreferencias))
                .timeout(timeout)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(escribir(cuerpo)))
                .build();

        JsonNode respuesta = ejecutar(pedido);
        String id = texto(respuesta, "id");
        String enlace = texto(respuesta, "init_point");
        if (id == null) {
            throw new MiException("Mercado Pago no devolvió el identificador de la preferencia");
        }
        return new Preferencia(id, enlace);
    }

    public PagoExterno consultarPago(String idPago) throws MiException {
        exigirConfiguracion();
        if (idPago == null || idPago.isBlank()) {
            throw new MiException("Falta el identificador del pago de Mercado Pago");
        }
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(urlBase + "/v1/payments/" + idPago))
                .timeout(timeout)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        JsonNode respuesta = ejecutar(pedido);
        return new PagoExterno(texto(respuesta, "id"), texto(respuesta, "status"),
                texto(respuesta, "external_reference"));
    }

    /** Valida el encabezado x-signature según el manifiesto que documenta Mercado Pago. */
    public boolean firmaValida(String firma, String requestId, String dataId) {
        if (!webhookConfigurado() || firma == null || requestId == null || requestId.isBlank()
                || dataId == null || dataId.isBlank()) {
            return false;
        }
        String ts = null;
        String v1 = null;
        for (String parte : firma.split(",")) {
            String[] par = parte.split("=", 2);
            if (par.length != 2) {
                continue;
            }
            String clave = par[0].trim();
            if ("ts".equals(clave)) {
                ts = par[1].trim();
            } else if ("v1".equals(clave)) {
                v1 = par[1].trim();
            }
        }
        if (ts == null || v1 == null) {
            return false;
        }
        String manifiesto = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";";
        String calculada = hmacHex(secretoWebhook, manifiesto);
        return calculada != null
                && MessageDigest.isEqual(calculada.getBytes(StandardCharsets.UTF_8), v1.getBytes(StandardCharsets.UTF_8));
    }

    public String getUrlNotificacion() {
        return urlNotificacion;
    }

    private void exigirConfiguracion() throws MiException {
        if (!estaConfigurado()) {
            throw new MiException("Mercado Pago no está configurado en el servidor: falta el access token");
        }
    }

    private JsonNode ejecutar(HttpRequest pedido) throws MiException {
        try {
            HttpResponse<String> respuesta = http.send(pedido, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() >= 300) {
                LOG.warn("Mercado Pago respondió {}: {}", respuesta.statusCode(), recortar(respuesta.body()));
                throw new MiException("Mercado Pago rechazó la operación (código " + respuesta.statusCode() + ")");
            }
            return json.readTree(respuesta.body());
        } catch (IOException e) {
            throw new MiException("No se pudo comunicar con Mercado Pago");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MiException("La comunicación con Mercado Pago fue interrumpida");
        } catch (RuntimeException e) {
            throw new MiException("No se pudo interpretar la respuesta de Mercado Pago");
        }
    }

    private String escribir(Object cuerpo) throws MiException {
        try {
            return json.writeValueAsString(cuerpo);
        } catch (RuntimeException e) {
            throw new MiException("No se pudo preparar la solicitud a Mercado Pago");
        }
    }

    private static String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        return valor == null || valor.isNull() ? null : valor.asText();
    }

    private static String hmacHex(String secreto, String mensaje) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            LOG.error("No se pudo calcular la firma del webhook", e);
            return null;
        }
    }

    private static String recortar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= 300 ? texto : texto.substring(0, 300);
    }

    /** Preferencia creada en Mercado Pago. */
    public record Preferencia(String id, String enlacePago) {
    }

    /** Estado del pago consultado en Mercado Pago. */
    public record PagoExterno(String id, String estado, String referenciaOperacion) {

        public boolean aprobado() {
            return "approved".equalsIgnoreCase(estado);
        }
    }
}
