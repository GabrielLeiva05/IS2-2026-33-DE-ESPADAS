package com.ejercicioIntegrador.dulcemayor.dto;

/** Alta y modificación de proveedores (crearProveedor(razonSocial, cuit, idDireccion) del diagrama). */
public record ProveedorRequestDTO(String razonSocial, String cuit, DireccionRequestDTO direccion) {
}
