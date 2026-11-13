package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

/** Incluye la cadena completa de ubicación para poder mostrarla sin otra consulta. */
public record DireccionResponseDTO(String id, String calle, String numeracion, String barrio, String manzanaPiso,
                                   String casaDepartamento, String referencia, Double latitud, Double longitud,
                                   String idLocalidad, String localidad, String codigoPostal,
                                   String idDepartamento, String departamento,
                                   String idProvincia, String provincia,
                                   String idPais, String pais, boolean eliminado) implements BaseResponseDTO {
}
