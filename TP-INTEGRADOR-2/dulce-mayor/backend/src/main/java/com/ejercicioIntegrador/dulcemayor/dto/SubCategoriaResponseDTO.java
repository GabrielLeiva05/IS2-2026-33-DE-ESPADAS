package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

public record SubCategoriaResponseDTO(String id, String nombre, String idCategoria, String nombreCategoria,
                                      boolean eliminado) implements BaseResponseDTO {
}
