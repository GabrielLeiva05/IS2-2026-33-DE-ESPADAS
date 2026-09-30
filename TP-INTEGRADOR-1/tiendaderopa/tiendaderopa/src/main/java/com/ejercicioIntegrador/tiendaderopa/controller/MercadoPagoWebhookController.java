package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.service.MercadoPagoCheckoutService;
import com.ejercicioIntegrador.tiendaderopa.service.MercadoPagoWebhookSignature;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MercadoPagoWebhookController {

    private final MercadoPagoCheckoutService checkoutService;
    private final MercadoPagoWebhookSignature signatureValidator;

    @Value("${mercadopago.webhook-secret:}")
    private String webhookSecret;

    public MercadoPagoWebhookController(MercadoPagoCheckoutService checkoutService,
            MercadoPagoWebhookSignature signatureValidator) {
        this.checkoutService = checkoutService;
        this.signatureValidator = signatureValidator;
    }

    @PostMapping("/webhooks/mercadopago")
    public ResponseEntity<Void> notificacion(
            @RequestParam(name = "data.id", required = false) String queryPaymentId,
            @RequestHeader(name = "x-signature", required = false) String signature,
            @RequestHeader(name = "x-request-id", required = false) String requestId,
            @RequestBody(required = false) String body) throws Exception {
                
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        String paymentId = queryPaymentId;
        String type = "";
        if (body != null && !body.isBlank()) {
            try {
                JsonObject notification = JsonParser.parseString(body).getAsJsonObject();
                type = notification.has("type") ? notification.get("type").getAsString() : "";
                JsonElement data = notification.get("data");
                if ((paymentId == null || paymentId.isBlank()) && data != null && data.isJsonObject()) {
                    JsonElement id = data.getAsJsonObject().get("id");
                    paymentId = id == null ? null : id.getAsString();
                }
            } catch (JsonParseException | IllegalStateException ex) {
                return ResponseEntity.badRequest().build();
            }
        }
        if (paymentId == null || paymentId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (!signatureValidator.validar(paymentId, requestId, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if ("payment".equals(type) || type.isBlank()) {
            checkoutService.procesarWebhook(paymentId);
        }
        return ResponseEntity.ok().build();
    }
}