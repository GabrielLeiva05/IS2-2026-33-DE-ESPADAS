package com.ejercicioIntegrador.tiendaderopa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ActualizarPerfilDTO(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @Size(max = 40) String sexo,
        @NotNull @Past @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
        @NotBlank @Pattern(regexp = "\\d{6,15}") String telefono,
        @NotBlank @Size(max = 20) String codigoPostal,
        @NotBlank String barrio,
        @NotBlank String calle,
        @NotBlank String numeracion,
        String manzanaPiso,
        String casaDepartamento,
        String referencia,
        @NotBlank String localidadId) {
}