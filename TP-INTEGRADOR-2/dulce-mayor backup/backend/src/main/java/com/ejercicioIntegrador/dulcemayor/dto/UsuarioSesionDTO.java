package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;

/** Datos del usuario autenticado que necesitan las aplicaciones cliente. Sin clave. */
public record UsuarioSesionDTO(String idUsuario, String correo, RolUsuario rol, boolean debeCambiarClave,
                               ProveedorAutenticacion proveedorAutenticacion, String nombre, String apellido) {
}
