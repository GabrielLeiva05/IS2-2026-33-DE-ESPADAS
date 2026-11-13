package com.dulcemayor.institucional.dto;

/** Mensaje que el navegador manda al asistente y se reenvía tal cual a /api/presupuestos/chat. */
public record MensajeAsistenteDTO(String idConversacion, String mensaje, String idProductoElegido) {
}
