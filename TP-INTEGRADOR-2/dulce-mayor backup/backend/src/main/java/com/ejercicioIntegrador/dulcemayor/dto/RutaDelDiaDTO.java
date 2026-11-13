package com.ejercicioIntegrador.dulcemayor.dto;

/**
 * Lo que necesita la app móvil: la ruta del día, el siguiente punto sugerido (el pendiente más cercano)
 * y el trayecto desde la ubicación informada por los pendientes en orden.
 */
public record RutaDelDiaDTO(RutaResponseDTO ruta, PuntoRutaDTO siguiente, Double distanciaSiguienteKm,
                            TrayectoDTO trayecto) {
}
