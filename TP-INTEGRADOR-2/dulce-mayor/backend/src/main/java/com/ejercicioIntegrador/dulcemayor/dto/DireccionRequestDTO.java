package com.ejercicioIntegrador.dulcemayor.dto;

/** Latitud y longitud son opcionales para la dirección de una persona. */
public record DireccionRequestDTO(String calle, String numeracion, String barrio, String manzanaPiso,
                                  String casaDepartamento, String referencia, Double latitud, Double longitud,
                                  String idLocalidad) {
}
