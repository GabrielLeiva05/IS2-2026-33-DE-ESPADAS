package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

public record SucursalResponseDTO(String id, String nombre, String telefono, String emailContacto, String horario,
                                  DireccionResponseDTO direccion, boolean activa, boolean eliminado)
        implements BaseResponseDTO {
}
