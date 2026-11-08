package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El cliente de Mercado Pago usa Checkout API: QR dinámico y pago con tarjeta tokenizada, sin
 * preferencias ni redirección.
 */
class ClienteMercadoPagoTest {

    private static final String SECRETO = "secreto-de-pruebas";

    @Test
    void sinAccessTokenLaIntegracionNoEstaConfiguradaYNoInventaCobros() {
        ClienteMercadoPago cliente = cliente("", SECRETO, "");

        assertFalse(cliente.estaConfigurado());
        assertFalse(cliente.qrConfigurado());
        MiException qr = assertThrows(MiException.class,
                () -> cliente.crearOrdenQr("ref-1", new BigDecimal("10.00"), "Pedido"));
        assertTrue(qr.getMessage().contains("no está configurado"));
        assertThrows(MiException.class, () -> cliente.consultarPago("123"));
        assertThrows(MiException.class, () -> cliente.crearPagoConTarjeta("ref-1", new BigDecimal("10.00"),
                new ClienteMercadoPago.DatosTarjeta("tok", "visa", 1, null, "a@b.test", "DNI", "12345678"),
                "Pedido"));
    }

    @Test
    void elQrNecesitaAdemasElVendedorYLaCaja() {
        ClienteMercadoPago conToken = cliente("token", SECRETO, "");
        assertTrue(conToken.estaConfigurado());
        assertFalse(conToken.qrConfigurado());
        MiException error = assertThrows(MiException.class,
                () -> conToken.crearOrdenQr("ref-1", new BigDecimal("10.00"), "Pedido"));
        assertTrue(error.getMessage().contains("MERCADOPAGO_USER_ID"));

        ClienteMercadoPago completo = cliente("token", SECRETO, "123456");
        assertTrue(completo.qrConfigurado());
        assertTrue(completo.webhookConfigurado());
        assertTrue(completo.estaConfigurado());
    }

    @Test
    void laFirmaDelWebhookSeValidaConElManifiestoDocumentado() {
        ClienteMercadoPago cliente = cliente("token", SECRETO, "123456");
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
        ClienteMercadoPago cliente = cliente("token", "", "123456");
        String ts = "1704908010";
        String firma = "ts=" + ts + ",v1=" + hmac("id:1;request-id:r;ts:" + ts + ";");

        assertFalse(cliente.webhookConfigurado());
        assertFalse(cliente.firmaValida(firma, "r", "1"));
    }

    private ClienteMercadoPago cliente(String token, String secreto, String userId) {
        return new ClienteMercadoPago("https://api.mercadopago.test", token, secreto,
                "https://dulcemayor.test/api/pagos/mercadopago/notificacion", userId, "CAJA01",
                "/v1/payments", "/instore/orders/qr/seller/collectors/{userId}/pos/{posId}/qrs", 2000);
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
