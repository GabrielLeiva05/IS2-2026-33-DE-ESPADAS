package com.dulcemayor.institucional.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos del formulario de cambio de contraseña (obligatorio o voluntario). */
@Getter
@Setter
@NoArgsConstructor
public class CambioClaveFormularioDTO {

    private String claveActual;
    private String nuevaClave;
    private String confirmarClave;
}
