package com.ejercicioIntegrador.dulcemayor.dto;

/**
 * Resultado de geocodificación (dirección → coordenadas) o geocodificación inversa (coordenadas → dirección
 * aproximada). Los campos de dirección pueden venir vacíos: el preventista los confirma o corrige.
 */
public record GeocodificacionDTO(double latitud, double longitud, String descripcion, String calle,
                                 String numeracion, String barrio, String localidad, String departamento,
                                 String provincia, String codigoPostal) {
}
