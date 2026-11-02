package com.dulcemayor.institucional.dto;

/** Cuerpo de POST /api/auth/oauth2/canje: el código de un solo uso que dejó la API tras Google/Facebook. */
public record CanjeSolicitudDTO(String codigo) {
}
