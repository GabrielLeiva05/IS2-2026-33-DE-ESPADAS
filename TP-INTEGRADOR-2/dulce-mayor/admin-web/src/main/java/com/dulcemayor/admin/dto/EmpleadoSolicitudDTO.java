package com.dulcemayor.admin.dto;

/** Alta o modificación de un empleado (EmpleadoRequestDTO del backend). Las fechas viajan en formato ISO. */
public record EmpleadoSolicitudDTO(String nombre, String apellido, String fechaNacimiento,
                                   String tipoDocumento, String numeroDocumento, String correo, String rol,
                                   String legajo, String fechaIngreso, DireccionSolicitudDTO direccion,
                                   String zona, String vehiculo, String patente, String idImagen) {
}
