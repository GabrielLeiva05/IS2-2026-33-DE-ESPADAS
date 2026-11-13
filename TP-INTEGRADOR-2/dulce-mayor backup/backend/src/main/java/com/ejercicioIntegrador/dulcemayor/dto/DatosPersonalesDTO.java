package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

/** Datos personales que un cliente puede editar de sí mismo (sin correo, clave ni rol). */
public record DatosPersonalesDTO(String nombre, String apellido, LocalDate fechaNacimiento,
                                 TipoDocumento tipoDocumento, String numeroDocumento) {
}
