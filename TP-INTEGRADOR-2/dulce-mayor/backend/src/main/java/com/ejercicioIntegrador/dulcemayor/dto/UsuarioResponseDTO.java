package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;

/** Sin la clave: nunca se devuelve ni siquiera el hash. */
public record UsuarioResponseDTO(String id, String nombreUsuario, RolUsuario rol, boolean debeCambiarClave,
                                 ProveedorAutenticacion proveedorAutenticacion,
                                 boolean eliminado) implements BaseResponseDTO {
}
