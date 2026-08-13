package org.example;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    @OneToMany(mappedBy = "autor",cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<Libro> librosEscritos = new HashSet<>();

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<Libro> getLibrosEscritos() {
        return librosEscritos;
    }

    public void setLibrosEscritos(Set<Libro> librosEscritos) {
        this.librosEscritos = librosEscritos;
    }

}
