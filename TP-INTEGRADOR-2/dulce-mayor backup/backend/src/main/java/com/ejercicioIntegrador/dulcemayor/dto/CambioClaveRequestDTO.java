package com.ejercicioIntegrador.dulcemayor.dto;

/** modificarClave(claveActual, nuevaClave, confirmarClave) del diagrama. */
public record CambioClaveRequestDTO(String claveActual, String nuevaClave, String confirmarClave) {
}
