package ar.com.biblioteca.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLDelete;

/**
 * Producto concreto de la Fábrica: un libro electrónico.
 * Dato propio: el formato del archivo (PDF, EPUB, ...).
 */
@Entity
@DiscriminatorValue(LibroDigital.TIPO)
@SQLDelete(sql = "UPDATE libro SET eliminado = true WHERE id = ?")
public class LibroDigital extends Libro {

    public static final String TIPO = "DIGITAL";

    @Size(max = 20, message = "El formato no puede superar los 20 caracteres")
    @Column(length = 20)
    private String formato;

    public LibroDigital() {
    }

    @Override
    public String getTipo() {
        return TIPO;
    }

    @Override
    public void copiarDatosPropios(Libro origen) {
        this.formato = ((LibroDigital) origen).getFormato();
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }
}
