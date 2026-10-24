package com.uncuyo.tp1_ej4.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "estudios")
@Getter
@Setter
public class Estudio extends EntidadBase {
    private String nombre;

    @OneToMany(mappedBy = "estudio")
    @JsonIgnore
    private List<Videojuego> videojuegos;

    public Estudio() {
    }

    public Estudio(long id, String nombre, boolean activo, List<Videojuego> videojuegos) {
        super(id, activo);
        this.nombre = nombre;
        this.videojuegos = videojuegos;
    }
}
