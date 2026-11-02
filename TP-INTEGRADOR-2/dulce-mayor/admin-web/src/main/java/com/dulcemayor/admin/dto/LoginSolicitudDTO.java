package com.dulcemayor.admin.dto;

/** Cuerpo de POST /api/auth/login. */
public record LoginSolicitudDTO(String correo, String clave) {
}
