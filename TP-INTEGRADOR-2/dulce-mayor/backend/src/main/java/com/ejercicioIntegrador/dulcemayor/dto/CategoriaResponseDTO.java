package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

public record CategoriaResponseDTO(String id, String nombre, boolean eliminado) implements BaseResponseDTO {
}
