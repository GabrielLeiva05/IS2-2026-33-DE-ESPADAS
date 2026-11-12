package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

/**
 * DTO INTERNO de ServicioCliente (alta y modificación). No lo recibe ningún controlador:
 * el registro público usa RegistroClienteDTO y la edición de datos usa DatosPersonalesDTO.
 * Así un cliente nunca puede elegir el proveedor de autenticación ni el rol de su cuenta.
 */
public record ClienteRequestDTO(String nombre, String apellido, LocalDate fechaNacimiento,
                                TipoDocumento tipoDocumento, String numeroDocumento,
                                String correo, String clave, ProveedorAutenticacion proveedorAutenticacion,
                                boolean claveTemporal) {

    public ClienteRequestDTO(String nombre, String apellido, LocalDate fechaNacimiento,
                             TipoDocumento tipoDocumento, String numeroDocumento,
                             String correo, String clave, ProveedorAutenticacion proveedorAutenticacion) {
        this(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento, correo, clave,
                proveedorAutenticacion, false);
    }
}
