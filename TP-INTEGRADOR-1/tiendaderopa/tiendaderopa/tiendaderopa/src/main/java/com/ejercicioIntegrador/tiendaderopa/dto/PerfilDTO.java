package com.ejercicioIntegrador.tiendaderopa.dto;

import java.time.LocalDate;

public record PerfilDTO(
        String email,
        String nombre,
        String apellido,
        String sexo,
        LocalDate fechaNacimiento,
        String tipoDocumento,
        String documento,
        String telefono,
        String codigoPostal,
        String barrio,
        String calle,
        String numeracion,
        String manzanaPiso,
        String casaDepartamento,
        String referencia,
        String localidadId,
        String localidad,
        String departamento,
        String provincia,
        String pais) {
}