package com.ejercicioIntegrador.dulcemayor.dto;

/**
 * Mensaje del cliente al asistente de presupuestos.
 *
 * @param idConversacion    null para empezar una conversación nueva
 * @param idProductoElegido opcional: el producto que eligió entre las opciones que le ofreció el asistente
 */
public record MensajePresupuestoDTO(String idConversacion, String mensaje, String idProductoElegido) {
}
