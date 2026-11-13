package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cliente tal como lo devuelve la API (ClienteResponseDTO). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClienteDTO {

    private String id;
    private String nombre;
    private String apellido;
    private String fechaNacimiento;
    private String tipoDocumento;
    private String numeroDocumento;
    private String correo;
    private String proveedorAutenticacion;
    private boolean eliminado;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getIniciales() {
        String n = nombre == null || nombre.isBlank() ? "" : nombre.substring(0, 1);
        String a = apellido == null || apellido.isBlank() ? "" : apellido.substring(0, 1);
        return (n + a).toUpperCase();
    }
}
