package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Respuesta de /api/auth/login y /api/auth/clave: el JWT y los datos del usuario. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SesionApiDTO {

    private String token;
    private String tipo;
    private long expiraEnSegundos;
    private UsuarioSesionApiDTO usuario;
}
