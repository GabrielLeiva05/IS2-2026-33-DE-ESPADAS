package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

/**
 * Registro público de clientes desde el sitio institucional (HU-09).
 * No tiene campo de rol: la cuenta se crea siempre con rol CLIENTE.
 * Documento y fecha de nacimiento son opcionales.
 */
public record RegistroClienteDTO(String nombre, String apellido, String correo, String clave,
                                 String confirmacionClave, TipoDocumento tipoDocumento, String numeroDocumento,
                                 LocalDate fechaNacimiento) {
}
