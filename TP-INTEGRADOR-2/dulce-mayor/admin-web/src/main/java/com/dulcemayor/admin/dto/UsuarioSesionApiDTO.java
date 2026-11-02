package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos del usuario autenticado que devuelve la API (nunca incluye la clave). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioSesionApiDTO {

    private String idUsuario;
    private String correo;
    private String rol;
    private boolean debeCambiarClave;
    private String proveedorAutenticacion;
    private String nombre;
    private String apellido;
}
