package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

public record PuntoVentaResponseDTO(String id, String idCliente, String nombreCliente, String apellidoCliente,
                                    String idPreventista, String nombreComercial, String telefono,
                                    String emailContacto, DireccionResponseDTO direccion, boolean eliminado)
        implements BaseResponseDTO {
}
