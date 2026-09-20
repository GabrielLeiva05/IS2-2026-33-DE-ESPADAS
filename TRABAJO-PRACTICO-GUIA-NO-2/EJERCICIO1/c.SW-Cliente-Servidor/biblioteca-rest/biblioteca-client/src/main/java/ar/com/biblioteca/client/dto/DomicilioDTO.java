package ar.com.biblioteca.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTO de Domicilio: id: Long, calle: String, numero: int (+ su Localidad). */
public class DomicilioDTO {

    private Long id;

    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 150, message = "La calle no puede superar los 150 caracteres")
    private String calle;

    @NotNull(message = "El número es obligatorio")
    @Min(value = 1, message = "El número debe ser mayor a cero")
    private Integer numero;

    /** Se inicializa para que el formulario pueda enlazar "domicilio.localidad.id". */
    private LocalidadDTO localidad = new LocalidadDTO();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public LocalidadDTO getLocalidad() {
        return localidad;
    }

    public void setLocalidad(LocalidadDTO localidad) {
        this.localidad = localidad;
    }

    /** Texto para la vista: "Calle 123, Localidad". No se envía al servidor. */
    @JsonIgnore
    public String getDireccion() {
        String loc = (localidad != null && localidad.getDenominacion() != null)
                ? ", " + localidad.getDenominacion() : "";
        return calle + " " + numero + loc;
    }
}
