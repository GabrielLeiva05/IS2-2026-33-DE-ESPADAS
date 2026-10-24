package org.example;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class Persona {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @OneToOne
    private Domicilio domicilio;

    public Persona() {}

    public Persona(Long id, String nombre, Domicilio domicilio) {
        this.id = id;
        this.nombre = nombre;
        this.domicilio = domicilio;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
    }

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", domicilio=" + domicilio +
                '}';
    }
}
