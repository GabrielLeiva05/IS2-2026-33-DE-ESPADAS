package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

/** Un turno de la conversación; deUsuario=false es una respuesta del asistente. */
public record MensajeChat(boolean deUsuario, String texto) {
}
