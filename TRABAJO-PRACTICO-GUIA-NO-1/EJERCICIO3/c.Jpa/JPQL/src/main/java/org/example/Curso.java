package org.example;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @ManyToMany(mappedBy = "cursos")
    private Set<Persona> inscriptos = new HashSet<>();

    public Curso(){}
    public Curso(String nombre) {
        this.nombre = nombre;
    }

    public void agregarInscriptos(Persona persona) {
        inscriptos.add(persona);

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<Persona> getInscriptos() {
        return inscriptos;
    }

    public void setInscriptos(Set<Persona> inscriptos) {
        this.inscriptos = inscriptos;
    }

    @Override
    public String toString() {
        return "Curso{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}