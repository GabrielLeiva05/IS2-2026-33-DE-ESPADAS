package ar.com.biblioteca.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@MappedSuperclass
public abstract class EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @JsonIgnore
    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean eliminado = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public abstract String descripcion();

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id + " [" + descripcion() + "]";
    }
}