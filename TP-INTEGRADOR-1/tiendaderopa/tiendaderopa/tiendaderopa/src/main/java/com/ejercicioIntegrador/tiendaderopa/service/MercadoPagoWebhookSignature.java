package com.ejercicioIntegrador.tiendaderopa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class MercadoPagoWebhookSignature {

    @Value("${mercadopago.webhook-secret:}")
    private String webhookSecret;

    public boolean validar(String dataId, String requestId, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()
                || dataId == null || dataId.isBlank()
                || requestId == null || requestId.isBlank()
                || signature == null || signature.isBlank()) {
            return false;
        }

        String timestamp = null;
        String receivedHash = null;
        for (String part : signature.split(",")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length != 2) {
                continue;
            }
            if ("ts".equals(pair[0])) {
                timestamp = pair[1];
            } else if ("v1".equals(pair[0])) {
                receivedHash = pair[1];
            }
        }
        if (timestamp == null || receivedHash == null) {
            return false;
        }

        try {
            String manifest = "id:" + dataId.toLowerCase() + ";request-id:" + requestId
                    + ";ts:" + timestamp + ";";
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = hmac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            byte[] received = HexFormat.of().parseHex(receivedHash);
            return MessageDigest.isEqual(expected, received);
        } catch (Exception ex) {
            return false;
        }
    }
}