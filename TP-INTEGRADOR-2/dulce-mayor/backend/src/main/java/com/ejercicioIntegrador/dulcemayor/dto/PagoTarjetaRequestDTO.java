package com.ejercicioIntegrador.dulcemayor.dto;

/**
 * Datos de la tarjeta <b>tokenizada en el navegador</b> con Mercado Pago Bricks (Checkout API).
 * El número y el código de seguridad nunca llegan al servidor: solo el token de un solo uso.
 */
public record PagoTarjetaRequestDTO(String token, String paymentMethodId, Integer installments, String issuerId,
                                    String email, String tipoDocumento, String numeroDocumento) {
}
