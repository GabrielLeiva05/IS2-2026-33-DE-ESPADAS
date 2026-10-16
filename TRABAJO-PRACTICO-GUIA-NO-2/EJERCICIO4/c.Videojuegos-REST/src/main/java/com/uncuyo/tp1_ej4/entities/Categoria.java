package com.uncuyo.tp1_ej4.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nombre;
    private boolean activo = true;

    @OneToMany(mappedBy = "categoria")
    @JsonIgnore
    private List<Videojuego> videojuegos;

    public Categoria() {
    }

    public Categoria(long id, String nombre, boolean activo, List<Videojuego> videojuegos) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
        this.videojuegos = videojuegos;
    }
}