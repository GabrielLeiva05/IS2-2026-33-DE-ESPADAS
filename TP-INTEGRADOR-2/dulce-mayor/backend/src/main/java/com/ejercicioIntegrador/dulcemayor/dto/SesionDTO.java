package com.ejercicioIntegrador.dulcemayor.dto;

/** Respuesta del login: el JWT y los datos del usuario. */
public record SesionDTO(String token, String tipo, long expiraEnSegundos, UsuarioSesionDTO usuario) {

    public static SesionDTO bearer(String token, long expiraEnSegundos, UsuarioSesionDTO usuario) {
        return new SesionDTO(token, "Bearer", expiraEnSegundos, usuario);
    }
}
