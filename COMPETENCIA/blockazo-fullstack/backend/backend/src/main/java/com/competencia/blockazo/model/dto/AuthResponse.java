package com.competencia.blockazo.model.dto;

/** Respuesta de register / login: el JWT y los datos del usuario. */
public record AuthResponse(String token, String tipo, UsuarioResponse usuario) {
    public AuthResponse(String token, UsuarioResponse usuario) {
        this(token, "Bearer", usuario);
    }
}
