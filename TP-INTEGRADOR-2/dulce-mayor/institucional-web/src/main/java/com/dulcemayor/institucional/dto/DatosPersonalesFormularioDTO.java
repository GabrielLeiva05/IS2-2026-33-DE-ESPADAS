package com.dulcemayor.institucional.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Formulario "Mis datos" del cliente. */
@Getter
@Setter
@NoArgsConstructor
public class DatosPersonalesFormularioDTO {

    private String nombre;
    private String apellido;
    private String fechaNacimiento;
    private String tipoDocumento;
    private String numeroDocumento;

    public static DatosPersonalesFormularioDTO desde(ClienteDTO cliente) {
        DatosPersonalesFormularioDTO formulario = new DatosPersonalesFormularioDTO();
        formulario.setNombre(cliente.getNombre());
        formulario.setApellido(cliente.getApellido());
        formulario.setFechaNacimiento(cliente.getFechaNacimiento());
        formulario.setTipoDocumento(cliente.getTipoDocumento());
        formulario.setNumeroDocumento(cliente.getNumeroDocumento());
        return formulario;
    }
}
