package com.ejercicioIntegrador.dulcemayor.service.correo;

/**
 * Datos de un correo ya armado, que recorre las capas del patrón Decorator.
 *
 * @param destino     dirección del destinatario
 * @param asunto      asunto
 * @param html        cuerpo en HTML
 * @param imagenQrPng imagen PNG opcional que el HTML muestra en línea con src="cid:{@value #ID_IMAGEN_QR}"
 *                    (el QR de Mercado Pago); null si el correo no lleva imagen
 */
public record MensajeCorreo(String destino, String asunto, String html, byte[] imagenQrPng) {

    /** Identificador con el que el HTML referencia la imagen en línea. */
    public static final String ID_IMAGEN_QR = "qrPago";
}
