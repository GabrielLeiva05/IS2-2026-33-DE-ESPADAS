package com.dulcemayor.institucional.dto;

/** Cuerpo de PUT /api/clientes/yo: solo datos personales (sin correo, clave ni rol). */
public record DatosPersonalesSolicitudDTO(String nombre, String apellido, String fechaNacimiento, String tipoDocumento,
                                          String numeroDocumento) {
}
