package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteMercadoPagoTest {

    private static final String SECRETO = "secreto-de-pruebas";

    @Test
    void sinAccessTokenLaIntegracionNoEstaConfiguradaYNoInventaPreferencias() {
        ClienteMercadoPago cliente = cliente("", SECRETO);

        assertFalse(cliente.estaConfigurado());
        MiException error = assertThrows(MiException.class,
                () -> cliente.crearPreferencia("ref-1", new BigDecimal("10.00"), "Pedido"));
        assertTrue(error.getMessage().contains("no está configurado"));
        assertThrows(MiException.class, () -> cliente.consultarPago("123"));
    }

    @Test
    void laFirmaDelWebhookSeValidaConElManifiestoDocumentado() {
        ClienteMercadoPago cliente = cliente("token", SECRETO);
        String ts = "1704908010";
        String requestId = "req-abc";
        String dataId = "123456";

        String valida = "ts=" + ts + ",v1=" + hmac("id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";");

        assertTrue(cliente.firmaValida(valida, requestId, dataId));
        assertFalse(cliente.firmaValida(valida, requestId, "999999"));
        assertFalse(cliente.firmaValida("ts=" + ts + ",v1=0000", requestId, dataId));
        assertFalse(cliente.firmaValida(null, requestId, dataId));
        assertFalse(cliente.firmaValida(valida, null, dataId));
        assertFalse(cliente.firmaValida(valida, requestId, null));
    }

    @Test
    void sinSecretoDeWebhookNingunaFirmaEsValida() {
        ClienteMercadoPago cliente = cliente("token", "");
        String ts = "1704908010";
        String firma = "ts=" + ts + ",v1=" + hmac("id:1;request-id:r;ts:" + ts + ";");

        assertFalse(cliente.webhookConfigurado());
        assertFalse(cliente.firmaValida(firma, "r", "1"));
    }

    @Test
    void losRequisitosDeConfiguracionSeReflejanEnLosGetters() {
        ClienteMercadoPago cliente = cliente("token", SECRETO);

        assertTrue(cliente.estaConfigurado());
        assertTrue(cliente.webhookConfigurado());
        assertEquals("https://dulcemayor.test/api/pagos/mercadopago/notificacion", cliente.getUrlNotificacion());
    }

    private ClienteMercadoPago cliente(String token, String secreto) {
        return new ClienteMercadoPago("https://api.mercadopago.test", token, secreto,
                "https://dulcemayor.test/api/pagos/mercadopago/notificacion", "/preferences", 2000);
    }

    private static String hmac(String mensaje) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRETO.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
