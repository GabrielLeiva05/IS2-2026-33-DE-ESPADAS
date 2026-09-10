package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name ="Persona")
public class Persona implements Serializable {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contacto> contactos = new ArrayList<>();

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private Usuario usuario;

    public void asignarUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            usuario.setPersona(this);
        }
    }

    public void removerUsuario() {
        if (this.usuario != null) {
            this.usuario.setPersona(null);
            this.usuario = null;
        }
    }
}