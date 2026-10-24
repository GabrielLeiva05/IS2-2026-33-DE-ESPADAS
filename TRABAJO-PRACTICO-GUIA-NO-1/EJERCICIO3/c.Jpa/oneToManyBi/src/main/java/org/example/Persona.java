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

    @OneToMany(mappedBy = "persona")
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
        if(domicilio!=null){
            domicilio.setPersona(this);
        }
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