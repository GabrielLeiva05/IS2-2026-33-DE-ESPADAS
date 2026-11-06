package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

/**
 * Alta de un cliente desde el celular del preventista: el propio cliente escribe su correo y su contraseña.
 * La dirección particular es opcional (Persona 1 ──> 0..1 Direccion) y nunca se usa como dirección comercial.
 * No tiene campo rol: la cuenta siempre es CLIENTE.
 */
public record ClienteCampoDTO(String nombre, String apellido, String correo, String clave, String confirmacionClave,
                              TipoDocumento tipoDocumento, String numeroDocumento, LocalDate fechaNacimiento,
                              DireccionRequestDTO direccion) {
}
