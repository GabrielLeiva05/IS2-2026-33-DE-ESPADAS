package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;

/**
 * Datos para crear una cuenta (crearUsuario del diagrama). Es un DTO INTERNO: no lo recibe ningún
 * controlador, lo arman ServicioEmpleado y ServicioCliente. Por eso un usuario nunca elige su rol
 * ni su proveedor desde el exterior.
 * clave: solo para clientes con registro local; a los empleados se les asigna la clave temporal.
 */
public record UsuarioRequestDTO(String nombreUsuario, RolUsuario rol, String clave,
                                ProveedorAutenticacion proveedorAutenticacion) {
}
