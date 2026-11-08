package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

public record ProveedorResponseDTO(String id, String razonSocial, String cuit, DireccionResponseDTO direccion,
                                   boolean eliminado) implements BaseResponseDTO {
}
