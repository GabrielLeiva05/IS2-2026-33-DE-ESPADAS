package ar.com.biblioteca.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Localidad: id: Long, denominacion: String.
 */
@Entity
@Table(name = "localidad")
public class Localidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La denominación es obligatoria")
    @Size(max = 100, message = "La denominación no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String denominacion;

    public Localidad() {
    }

    public Localidad(String denominacion) {
        this.denominacion = denominacion;
    }

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
