package com.ejercicioIntegrador.dulcemayor.dto.base;

import java.time.LocalDateTime;

/**
 * Respuesta uniforme de error para todos los endpoints de la API.
 */
public record ErrorDTO(int estado, String error, String mensaje, LocalDateTime fecha) {

    public static ErrorDTO de(int estado, String error, String mensaje) {
        return new ErrorDTO(estado, error, mensaje, LocalDateTime.now());
    }
}
