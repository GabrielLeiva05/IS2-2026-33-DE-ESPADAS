package com.dulcemayor.admin.dto;

/** Cuerpo de PUT /api/auth/clave. */
public record CambioClaveSolicitudDTO(String claveActual, String nuevaClave, String confirmarClave) {
}
