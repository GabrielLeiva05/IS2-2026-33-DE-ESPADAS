package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;

import java.time.LocalDate;

/**
 * Alta y modificación de empleados (HU-07).
 * - legajo y fechaIngreso son opcionales en el alta: si no se informan se genera el legajo y se usa la fecha de hoy.
 * - zona solo aplica a PREVENTISTA; vehiculo y patente solo a DISTRIBUIDOR.
 * - En la modificación no se pueden cambiar ni el rol ni el correo.
 */
public record EmpleadoRequestDTO(String nombre, String apellido, LocalDate fechaNacimiento,
                                 TipoDocumento tipoDocumento, String numeroDocumento, String correo,
                                 RolUsuario rol, String legajo, LocalDate fechaIngreso,
                                 DireccionRequestDTO direccion, String zona, String vehiculo, String patente,
                                 String idImagen) {
}
