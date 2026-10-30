package com.competencia.blockazo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cambio de clave desde el Perfil.
 * claveActual puede venir vacia SOLO si el usuario entro con Google y todavia no tiene clave.
 */
public record CambioClaveRequest(
        String claveActual,
        @NotBlank(message = "Debe indicar la nueva clave")
        @Size(min = 6, message = "La nueva clave debe tener al menos 6 caracteres") String claveNueva,
        @NotBlank(message = "Debe confirmar la nueva clave") String confirmacionClave
) {}
