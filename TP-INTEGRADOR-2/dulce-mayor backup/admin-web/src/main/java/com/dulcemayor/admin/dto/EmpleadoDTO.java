package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Empleado tal como lo devuelve la API (EmpleadoResponseDTO). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmpleadoDTO {

    private String id;
    private String nombre;
    private String apellido;
    private String fechaNacimiento;
    private String tipoDocumento;
    private String numeroDocumento;
    private String correo;
    private String rol;
    private boolean debeCambiarClave;
    private String legajo;
    private String fechaIngreso;
    private DireccionDTO direccion;
    private String vehiculo;
    private String patente;
    private String idImagen;
    private boolean eliminado;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getIniciales() {
        return (inicial(nombre) + inicial(apellido)).toUpperCase();
    }

    public String getRolLegible() {
        return Roles.legible(rol);
    }

    private static String inicial(String texto) {
        return texto == null || texto.isBlank() ? "" : texto.trim().substring(0, 1);
    }
}
