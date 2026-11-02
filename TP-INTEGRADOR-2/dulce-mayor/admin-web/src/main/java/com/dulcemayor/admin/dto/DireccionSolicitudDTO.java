package com.dulcemayor.admin.dto;

/** Dirección que se envía a la API (DireccionRequestDTO del backend). */
public record DireccionSolicitudDTO(String calle, String numeracion, String barrio, String manzanaPiso,
                                    String casaDepartamento, String referencia, Double latitud, Double longitud,
                                    String idLocalidad) {
}
