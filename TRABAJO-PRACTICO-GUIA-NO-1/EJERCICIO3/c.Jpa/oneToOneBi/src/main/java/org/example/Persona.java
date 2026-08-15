package org.example;

import jakarta.persistence.*;

@Entity
public class Persona {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @OneToOne
    private Domicilio domicilio;

    public Persona() {}

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

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", domicilio=" + domicilio +
                '}';
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
        if (domicilio != null) {
            domicilio.setPersona(this); // Mantener bidireccionalidad
        }

    }
}
