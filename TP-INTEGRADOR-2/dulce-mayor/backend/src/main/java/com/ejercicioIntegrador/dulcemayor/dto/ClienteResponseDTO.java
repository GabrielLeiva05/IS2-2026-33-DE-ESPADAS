package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

public record ClienteResponseDTO(String id, String nombre, String apellido, LocalDate fechaNacimiento,
                                 TipoDocumento tipoDocumento, String numeroDocumento, String correo,
                                 ProveedorAutenticacion proveedorAutenticacion,
                                 boolean eliminado) implements BaseResponseDTO {
}
