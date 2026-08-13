package org.example;

import jakarta.persistence.*;

import javax.security.sasl.AuthorizeCallback;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    //Observemos que ocurre cuando hacemos uso de LAZY y EAGER. (Por lo que entiendo, manyToOne por defecto es Eager)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id")
    private Autor autor;

    public Libro(){

    }

    public Libro(String nombre, Autor autor) {
        this.nombre = nombre;
        this.autor = autor;

        autor.getLibrosEscritos().add(this);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
