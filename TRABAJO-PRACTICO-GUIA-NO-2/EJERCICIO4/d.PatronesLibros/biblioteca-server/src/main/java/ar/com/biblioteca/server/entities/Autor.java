package ar.com.biblioteca.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLDelete;
import ar.com.biblioteca.server.prototype.Prototipo;
import org.hibernate.annotations.SQLRestriction;

/**
 * Autor: id: Long, nombre: String, apellido: String, biografia: String.
 * <p>
 * REFACTOR (patrón Prototype): Autor es el "ConcretePrototype": implementa {@link Prototipo}
 * y sabe devolver una copia de sí mismo con {@link #clonar()}.
 */
@Entity
@Table(name = "autor")
@SQLDelete(sql = "UPDATE autor SET eliminado = true WHERE id = ?")
@SQLRestriction("eliminado = false")
public class Autor extends EntidadBase implements Prototipo<Autor> {

    @NotBlank(message = "El nombre del autor es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido del autor es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellido;

    @Size(max = 2000, message = "La biografía no puede superar los 2000 caracteres")
    @Column(length = 2000)
    private String biografia;

    public Autor() {
    }

    public Autor(String nombre, String apellido, String biografia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.biografia = biografia;
    }

    /** Constructor de copia: lo usa clonar(). Copia los DATOS, no la identidad. */
    protected Autor(Autor original) {
        this.nombre = original.nombre;
        this.apellido = original.apellido;
        this.biografia = original.biografia;
    }

    /**
     * PROTOTYPE: devuelve un autor NUEVO con los mismos datos que este.
     * La copia no lleva id (la base le asigna uno al guardarla) y nace activa (eliminado = false):
     * es otro registro, no el mismo autor.
     */
    @Override
    public Autor clonar() {
        return new Autor(this);
    }

    @Override
    public String descripcion() {
        return nombre + " " + apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }
}
