package com.ejercicioIntegrador.dulcemayor.service.pagos;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente de la Checkout API de Mercado Pago para cobros con QR (sin redirección del cliente).
 *
 * Se usa la API de códigos QR dinámicos de la Checkout API:
 *   PUT /instore/orders/qr/seller/collectors/{user_id}/pos/{external_pos_id}/orders
 * que devuelve, entre otros datos, "qr_data" (contenido que se dibuja como QR) e "in_store_order_id".
 * El cliente escanea el QR desde el correo con la app de Mercado Pago; nunca sale de su flujo.
 *
 * El estado del pago se consulta desde el webhook:
 *   GET /merchant_orders/{id}  (topic=merchant_order)  o  GET /v1/payments/{id}  (topic=payment).
 *
 * Todas las credenciales se toman de application.properties / .env (MP_ACCESS_TOKEN, MP_USER_ID, ...).
 */
@Component
public class MercadoPagoQrCliente {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoQrCliente.class);
    private static final String RUTA_QR =
            "/instore/orders/qr/seller/collectors/{userId}/pos/{posId}/orders";

    private final RestClient rest;
    private final String accessToken;
    private final String userId;
    private final String externalPosId;
    private final String notificationUrl;

    public MercadoPagoQrCliente(@Value("${app.pagos.mercadopago.base-url:https://api.mercadopago.com}") String baseUrl,
                                @Value("${app.pagos.mercadopago.access-token:}") String accessToken,
                                @Value("${app.pagos.mercadopago.user-id:}") String userId,
                                @Value("${app.pagos.mercadopago.external-pos-id:CAJA01}") String externalPosId,
                                @Value("${app.pagos.mercadopago.notification-url:}") String notificationUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
        this.accessToken = accessToken;
        this.userId = userId;
        this.externalPosId = externalPosId;
        this.notificationUrl = notificationUrl;
    }

    /** ¿Hay credenciales mínimas configuradas? Si no, el pago con QR se rechaza con un mensaje claro. */
    public boolean estaConfigurado() {
        return accessToken != null && !accessToken.isBlank() && userId != null && !userId.isBlank();
    }

    /**
     * Crea una cobranza dinámica con QR y devuelve el contenido del QR y el id de la orden de Mercado Pago.
     * La referencia externa es el id del pedido: con ella el webhook encuentra el pago a acreditar.
     */
    public QrGenerado crearCobranza(String referencia, String titulo, String descripcion, double monto)
            throws MiException {
        if (!estaConfigurado()) {
            throw new MiException("Los pagos con QR no están configurados (faltan MP_ACCESS_TOKEN y MP_USER_ID)");
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", titulo);
        item.put("description", descripcion);
        item.put("quantity", 1);
        item.put("unit_price", monto);
        item.put("unit_measure", "unit");
        item.put("total_amount", monto);

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("external_reference", referencia);
        cuerpo.put("title", titulo);
        cuerpo.put("description", descripcion);
        cuerpo.put("total_amount", monto);
        cuerpo.put("items", List.of(item));
        if (notificationUrl != null && !notificationUrl.isBlank()) {
            cuerpo.put("notification_url", notificationUrl);
        }

        try {
            Map<?, ?> respuesta = rest.put()
                    .uri(RUTA_QR, userId, externalPosId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(Map.class);

            if (respuesta == null) {
                throw new MiException("Mercado Pago no devolvió la orden de cobro");
            }
            Object qrData = respuesta.get("qr_data");
            if (qrData == null || String.valueOf(qrData).isBlank()) {
                throw new MiException("Mercado Pago no devolvió el contenido del QR");
            }
            Object idOrden = respuesta.get("in_store_order_id");
            return new QrGenerado(String.valueOf(qrData), idOrden == null ? null : String.valueOf(idOrden));
        } catch (RestClientException e) {
            log.warn("Error al crear el QR de Mercado Pago: {}", e.getMessage());
            throw new MiException("No se pudo generar el QR de Mercado Pago: " + e.getMessage());
        }
    }

    /** Consulta una orden de la tienda (notificaciones con topic=merchant_order). */
    public Optional<EstadoConsulta> consultarMerchantOrder(String id) {
        try {
            Map<?, ?> respuesta = rest.get()
                    .uri("/merchant_orders/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
            if (respuesta == null) {
                return Optional.empty();
            }
            String referencia = texto(respuesta.get("external_reference"));
            String estado = "pending";
            String idPago = null;
            Object pagos = respuesta.get("payments");
            if (pagos instanceof List<?> lista) {
                for (Object elemento : lista) {
                    if (elemento instanceof Map<?, ?> pago) {
                        if ("approved".equalsIgnoreCase(texto(pago.get("status")))) {
                            estado = "approved";
                            idPago = texto(pago.get("id"));
                            break;
                        }
                        if (idPago == null) {
                            idPago = texto(pago.get("id"));
                            if (texto(pago.get("status")) != null) {
                                estado = texto(pago.get("status"));
                            }
                        }
                    }
                }
            }
            return Optional.of(new EstadoConsulta(estado, referencia, idPago));
        } catch (RestClientException e) {
            log.warn("No se pudo consultar la orden {} de Mercado Pago: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /** Consulta un pago puntual (notificaciones con topic=payment). */
    public Optional<EstadoConsulta> consultarPago(String id) {
        try {
            Map<?, ?> respuesta = rest.get()
                    .uri("/v1/payments/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
            if (respuesta == null) {
                return Optional.empty();
            }
            return Optional.of(new EstadoConsulta(texto(respuesta.get("status")),
                    texto(respuesta.get("external_reference")), texto(respuesta.get("id"))));
        } catch (RestClientException e) {
            log.warn("No se pudo consultar el pago {} de Mercado Pago: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : String.valueOf(valor);
    }

    /** Resultado de crear una cobranza con QR. */
    public record QrGenerado(String qrData, String idOrden) {
    }

    /** Estado informado por Mercado Pago para una orden o un pago. */
    public record EstadoConsulta(String estado, String referenciaExterna, String idPago) {
    }
}
