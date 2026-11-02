package com.dulcemayor.institucional.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Formulario de registro de clientes. El documento y la fecha de nacimiento son opcionales. */
@Getter
@Setter
@NoArgsConstructor
public class RegistroFormularioDTO {

    private String nombre;
    private String apellido;
    private String correo;
    private String clave;
    private String confirmacionClave;
    private String tipoDocumento;
    private String numeroDocumento;
    private String fechaNacimiento;
}
