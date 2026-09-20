package ar.com.biblioteca.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de Localidad: id: Long, denominacion: String. */
public class LocalidadDTO {

    private Long id;

    @NotBlank(message = "La denominación es obligatoria")
    @Size(max = 100, message = "La denominación no puede superar los 100 caracteres")
    private String denominacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }
}
