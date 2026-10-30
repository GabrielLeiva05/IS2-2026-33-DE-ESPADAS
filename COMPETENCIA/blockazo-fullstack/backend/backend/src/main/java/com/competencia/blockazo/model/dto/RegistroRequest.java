package com.competencia.blockazo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Alta de una persona (register). */
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El apellido es obligatorio") String apellido,
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener 7 u 8 digitos") String dni,
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy") LocalDate fechaNacimiento,
        @NotBlank(message = "La direccion es obligatoria") String direccion,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido") String correo,
        @NotBlank(message = "La clave es obligatoria")
        @Size(min = 6, message = "La clave debe tener al menos 6 caracteres") String clave,
        @NotBlank(message = "Debe confirmar la clave") String confirmacionClave
) {}
