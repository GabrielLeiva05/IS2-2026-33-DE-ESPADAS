package ar.com.biblioteca.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLDelete;

/**
 * Producto concreto de la Fábrica: un libro en papel.
 * Dato propio: dónde está guardado (estante / sector).
 * <p>
 * El @SQLDelete se repite acá porque Hibernate no lo hereda de Libro: sin él, la baja de un
 * LibroFisico sería un DELETE real en vez de la baja lógica (eliminado = true).
 */
@Entity
@DiscriminatorValue(LibroFisico.TIPO)
@SQLDelete(sql = "UPDATE libro SET eliminado = true WHERE id = ?")
public class LibroFisico extends Libro {

    public static final String TIPO = "FISICO";

    @Size(max = 100, message = "La ubicación no puede superar los 100 caracteres")
    @Column(length = 100)
    private String ubicacion;

    public LibroFisico() {
    }

    @Override
    public String getTipo() {
        return TIPO;
    }

    @Override
    public void copiarDatosPropios(Libro origen) {
        this.ubicacion = ((LibroFisico) origen).getUbicacion();
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
}
