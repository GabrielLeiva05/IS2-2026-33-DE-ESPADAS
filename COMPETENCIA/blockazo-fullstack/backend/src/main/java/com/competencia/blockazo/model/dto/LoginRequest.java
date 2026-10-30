package com.competencia.blockazo.model.dto;

import jakarta.validation.constraints.NotBlank;

/** Login: el correo es el nombre de usuario. */
public record LoginRequest(
        @NotBlank(message = "Debe indicar el correo") String correo,
        @NotBlank(message = "Debe indicar la clave") String clave
) {}
