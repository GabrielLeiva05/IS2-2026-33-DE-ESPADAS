package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaCliente;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompraProveedor;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class MercadoPagoCheckoutService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoCheckoutService.class);

    private final ServicioOrdenCompra ordenServicio;
    private final PreferenceClient preferenceClient = new PreferenceClient();
    private final PaymentClient paymentClient = new PaymentClient();

    private ServicioFacturaCliente svcFacturaCliente; // NUEVO

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${mercadopago.webhook-url:}")
    private String webhookUrl;

    @Value("${mercadopago.sandbox:false}")
    private boolean sandbox;

    public MercadoPagoCheckoutService(ServicioOrdenCompra ordenServicio) {
        this.ordenServicio = ordenServicio;
    }

    public String iniciarCheckout(String ordenId, String email) throws Exception {
        exigirCredenciales();
        OrdenCompra orden = ordenServicio.validarCarritoParaCheckout(ordenId, email);
        if (orden.getMercadoPagoInitPoint() != null && !orden.getMercadoPagoInitPoint().isBlank()) {
            return orden.getMercadoPagoInitPoint();
        }

        List<PreferenceItemRequest> items = orden.getDetalles().stream()
                .filter(detalle -> !detalle.isEliminado())
                .map(this::aItemPreferencia)
                .toList();
        String retorno = baseUrl.replaceAll("/$", "") + "/pago/resultado";
        var preferenceBuilder = PreferenceRequest.builder()
                .items(items)
                .payer(PreferencePayerRequest.builder().email(orden.getUsuario().getNombreUsuario()).build())
                .externalReference(orden.getId())
                .autoReturn("approved")
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(retorno)
                        .pending(retorno)
                        .failure(retorno)
                        .build())
                .statementDescriptor("ZERO");

        if (webhookUrl != null && !webhookUrl.isBlank()) {
            preferenceBuilder.notificationUrl(webhookUrl);
        }

        PreferenceRequest request = preferenceBuilder.build();
        MPRequestOptions options = MPRequestOptions.builder()
                .accessToken(accessToken)
                .customHeaders(Map.of("X-Idempotency-Key", "zero-checkout-" + orden.getId()))
                .build();

        Preference preference;
        try {
            preference = preferenceClient.create(request, options);
        } catch (MPApiException ex) {
            var apiResponse = ex.getApiResponse();
            String responseBody = apiResponse == null ? "(sin cuerpo)" : apiResponse.getContent();
            if (responseBody != null) {
                responseBody = responseBody.replaceAll("\\s+", " ");
                if (responseBody.length() > 2000) {
                    responseBody = responseBody.substring(0, 2000);
                }
            }
            logger.error("Mercado Pago rechazó la preferencia. HTTP {}. Respuesta: {}",
                    ex.getStatusCode(), responseBody);
            throw new IllegalStateException(
                    "Mercado Pago rechazó la solicitud (HTTP " + ex.getStatusCode()
                            + "). Consultá el log del servidor.", ex);
        }
        String checkoutUrl = sandbox ? preference.getSandboxInitPoint() : preference.getInitPoint();
        if (preference.getId() == null || checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalStateException("Mercado Pago no devolvió una URL de checkout válida");
        }

        ordenServicio.registrarPreferenciaMercadoPago(
                orden.getId(), email, preference.getId(), checkoutUrl);
        return checkoutUrl;
    }

    public OrdenCompra procesarRetorno(String ordenId, String paymentId, String email) throws Exception {
        exigirCredenciales();
        OrdenCompra orden = ordenServicio.buscarAccesibleParaUsuario(ordenId, email, false);
        if (paymentId == null || paymentId.isBlank() || "null".equalsIgnoreCase(paymentId)) {
            return orden;
        }
        return consultarYAplicarPago(paymentId, orden);
    }

    public void procesarWebhook(String paymentId) throws Exception {
        exigirCredenciales();
        Payment payment = obtenerPago(paymentId);
        String externalReference = payment.getExternalReference();
        if (externalReference == null || externalReference.isBlank()) {
            throw new IllegalArgumentException("El pago no tiene referencia de orden");
        }
        OrdenCompra orden = ordenServicio.buscarPorId(externalReference);
        consultarYAplicarPago(paymentId, orden);
    }

    private OrdenCompra consultarYAplicarPago(String paymentId, OrdenCompra orden) throws Exception {
        Payment payment = obtenerPago(paymentId);
        if (!orden.getId().equals(payment.getExternalReference())) {
            throw new IllegalArgumentException("El pago no corresponde a la orden indicada");
        }
        if (orden.getMercadoPagoPreferenceId() == null) {
            throw new IllegalStateException("La orden no tiene una preferencia Mercado Pago registrada");
        }
        if (!"ARS".equals(payment.getCurrencyId())) {
            throw new IllegalArgumentException("La moneda del pago no coincide con ARS");
        }
        BigDecimal totalOrden = BigDecimal.valueOf(orden.getTotal()).setScale(2, RoundingMode.HALF_UP);
        if (payment.getTransactionAmount() == null
                || payment.getTransactionAmount().compareTo(totalOrden) != 0) {
            throw new IllegalArgumentException("El importe del pago no coincide con el total de la orden");
        }

        OrdenCompra actualizada =  ordenServicio.registrarResultadoMercadoPago(
                orden.getId(), paymentId, payment.getStatus());
        if ("approved".equalsIgnoreCase(payment.getStatus()) && actualizada.getFacturaCliente() == null) {
            FacturaCliente factura = svcFacturaCliente.crearFacturaDesdeOrden(actualizada);
            ordenServicio.asociarFacturaCliente(actualizada.getId(), factura);
        }
        return actualizada;
    }

    private Payment obtenerPago(String paymentId) throws MPException, MPApiException {
        try {
            return paymentClient.get(Long.valueOf(paymentId), opcionesDeRequest());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El identificador del pago no es válido", ex);
        }
    }

    private PreferenceItemRequest aItemPreferencia(DetalleCompra detalle) {
        BigDecimal unitPrice = BigDecimal.valueOf(detalle.getSubtotal())
                .divide(BigDecimal.valueOf(detalle.getCantidad()), 2, RoundingMode.HALF_UP);
        return PreferenceItemRequest.builder()
                .id(detalle.getProducto().getCodigo())
                .title(detalle.getProducto().getNombre())
                .description(detalle.getProducto().getDescripcion())
                .quantity(detalle.getCantidad())
                .unitPrice(unitPrice)
                .currencyId("ARS")
                .build();
    }

    private MPRequestOptions opcionesDeRequest() {
        return MPRequestOptions.builder().accessToken(accessToken).build();
    }

    private void exigirCredenciales() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Falta configurar MERCADOPAGO_ACCESS_TOKEN");
        }
    }
}