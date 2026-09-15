package com.ejercicioIntegrador.tiendaderopa.model;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.DataInput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name ="Persona")
public class Persona implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Direccion> direcciones = new ArrayList<>();

    @Column(nullable = false)
    private Date fechaNacimiento;

    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false)
    private String documento;

    public Persona(String nombre, String apellido, Date fechaNacimiento, String documento, TipoDocumento tipoDocumento) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.documento = documento;
        this.tipoDocumento = tipoDocumento;
    }

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