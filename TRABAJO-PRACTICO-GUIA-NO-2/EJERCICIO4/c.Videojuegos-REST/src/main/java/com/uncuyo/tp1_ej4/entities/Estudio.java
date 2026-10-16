package com.uncuyo.tp1_ej4.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "estudios")
@Getter
@Setter
public class Estudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nombre;
    private boolean activo = true;

    @OneToMany(mappedBy = "estudio")
    @JsonIgnore
    private List<Videojuego> videojuegos;

    public Estudio() {
    }

    public Estudio(long id, String nombre, boolean activo, List<Videojuego> videojuegos) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
        this.videojuegos = videojuegos;
    }
}
