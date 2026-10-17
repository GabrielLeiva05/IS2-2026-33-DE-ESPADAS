package com.incisog.Clinica.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class Usuario extends BaseEntity {

    private String nombreUsuario;

    // WRITE_ONLY: Jackson la LEE del JSON que llega (para crear o hacer login),
    // pero NUNCA la escribe en el JSON de respuesta. Así la clave no sale de la API.
    // (En el refactor esto lo va a resolver el Adapter/DTO.)
    // Nota: se guarda en texto plano; aceptable para el TP, no para un sistema real.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String clave;

    public Usuario() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }
}
