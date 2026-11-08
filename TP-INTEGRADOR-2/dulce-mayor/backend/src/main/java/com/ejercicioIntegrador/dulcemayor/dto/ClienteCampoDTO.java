package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

/**
 * Alta de un cliente desde el celular del preventista. No lleva contraseña: la cuenta se crea con la clave
 * temporal "dulcemayor" y el cliente debe cambiarla en su primer ingreso al sitio (corrección Ola 1).
 * Documento y fecha de nacimiento son obligatorios en este formulario.
 * La dirección particular es opcional (Persona 1 ──> 0..1 Direccion) y nunca se usa como dirección comercial.
 * No tiene campo rol: la cuenta siempre es CLIENTE.
 */
public record ClienteCampoDTO(String nombre, String apellido, String correo,
                              TipoDocumento tipoDocumento, String numeroDocumento, LocalDate fechaNacimiento,
                              DireccionRequestDTO direccion) {
}
