package com.ejercicioIntegrador.dulcemayor.dto;

/** Resultado de una parada: motivo del intento fallido u observación de la entrega. */
public record CerrarParadaRequestDTO(String motivo, String observacion) {
}
