package org.example;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @OneToMany (cascade = {CascadeType.PERSIST,CascadeType.MERGE},fetch = FetchType.LAZY)
    @JoinColumn(name="persona_id")
    private List<Domicilio> domicilios = new ArrayList<>();

    public Persona() {}

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Domicilio> getDomicilios() {
        return domicilios;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDomicilios(List<Domicilio> domicilios) {
        this.domicilios = domicilios;
    }

    public void addDomicilio(Domicilio domicilio) {
        this.domicilios.add(domicilio);
    }

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", domicilios=" + domicilios +
                '}';
    }
}
