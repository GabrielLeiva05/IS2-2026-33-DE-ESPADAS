package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Registro de un correo enviado o simulado. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorreoAdminDTO {

    private String id;
    private String destinatario;
    private String asunto;
    private String tipo;
    private String estado;
    private String detalle;
    private String fechaCreacion;

    public String getFechaReferencia() {
        return fechaCreacion == null ? "" : fechaCreacion.replace("T", " ").substring(0, Math.min(16, fechaCreacion.length()));
    }

    public String getClaseEstado() {
        if (estado == null) {
            return "insignia-gris";
        }
        return switch (estado) {
            case "ENVIADO" -> "insignia-verde";
            case "FALLIDO" -> "insignia-roja";
            default -> "insignia-ambar";
        };
    }
}
