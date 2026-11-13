package com.dulcemayor.institucional.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos del formulario de ingreso de clientes. */
@Getter
@Setter
@NoArgsConstructor
public class LoginFormularioDTO {

    private String correo;
    private String clave;
}
