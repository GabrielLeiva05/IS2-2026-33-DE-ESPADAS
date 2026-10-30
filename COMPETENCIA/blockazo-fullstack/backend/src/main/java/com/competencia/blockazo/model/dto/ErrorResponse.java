package com.competencia.blockazo.model.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(int status, String mensaje, Map<String, String> errores, LocalDateTime fecha) {
    public ErrorResponse(int status, String mensaje) {
        this(status, mensaje, null, LocalDateTime.now());
    }
    public ErrorResponse(int status, String mensaje, Map<String, String> errores) {
        this(status, mensaje, errores, LocalDateTime.now());
    }
}
