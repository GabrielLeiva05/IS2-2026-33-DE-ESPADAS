package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

public record LocalidadResponseDTO(String id, String nombre, String codigoPostal, String idDepartamento,
                                   String nombreDepartamento, boolean eliminado) implements BaseResponseDTO {
}
