package com.incisog.Clinica.logic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Forma del usuario PARA LA API.
 * La clave se puede RECIBIR (alta, login, cambio de clave) pero nunca se ENVÍA:
 * esa regla es de la API, por eso la anotación vive acá y no en la entidad Usuario.
 */
public class UsuarioDTO {

    private String id;
    private String nombreUsuario;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String clave;

    public UsuarioDTO() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}