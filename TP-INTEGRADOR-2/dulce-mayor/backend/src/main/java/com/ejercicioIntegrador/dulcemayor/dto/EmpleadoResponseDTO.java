package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

public record EmpleadoResponseDTO(String id, String nombre, String apellido, LocalDate fechaNacimiento,
                                  TipoDocumento tipoDocumento, String numeroDocumento, String correo,
                                  RolUsuario rol, boolean debeCambiarClave, String legajo, LocalDate fechaIngreso,
                                  DireccionResponseDTO direccion, String vehiculo, String patente,
                                  String idImagen, boolean eliminado) implements BaseResponseDTO {
}
