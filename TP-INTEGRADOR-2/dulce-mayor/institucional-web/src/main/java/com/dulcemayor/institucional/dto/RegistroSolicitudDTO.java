package com.dulcemayor.institucional.dto;

/** Cuerpo de POST /api/auth/registro (HU-09). No tiene campo rol: la API siempre asigna CLIENTE. */
public record RegistroSolicitudDTO(String nombre, String apellido, String correo, String clave,
                                   String confirmacionClave, String tipoDocumento, String numeroDocumento,
                                   String fechaNacimiento) {
}
