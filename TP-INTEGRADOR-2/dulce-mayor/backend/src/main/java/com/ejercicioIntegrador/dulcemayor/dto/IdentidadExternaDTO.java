package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;

/**
 * Identidad devuelta por Google o Facebook, ya leída por el manejador OAuth2.
 * correoVerificado: Google lo informa (email_verified); Facebook no, por eso llega en false.
 */
public record IdentidadExternaDTO(String correo, boolean correoVerificado, String nombre, String apellido,
                                  ProveedorAutenticacion proveedor) {
}
