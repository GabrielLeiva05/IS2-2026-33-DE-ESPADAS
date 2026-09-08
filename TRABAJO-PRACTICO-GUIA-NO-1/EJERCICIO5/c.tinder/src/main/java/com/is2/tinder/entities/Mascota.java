package com.is2.tinder.entities;

import java.time.LocalDateTime;

import com.is2.tinder.enumerations.Sexo;
import com.is2.tinder.enumerations.Tipo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String nombre;

    private LocalDateTime alta;
    private LocalDateTime baja;

    @ManyToOne
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    @OneToOne
    private Foto foto;

    public Foto getFoto() {
        return foto;
    }
    public void setFoto(Foto foto) {
        this.foto = foto;
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public Sexo getSexo() {
        return sexo;
    }
    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
    public Tipo getTipo() {
        return tipo;
    }
    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public LocalDateTime getAlta() {
        return alta;
    }
    public void setAlta(LocalDateTime alta) {
        this.alta = alta;
    }
    public LocalDateTime getBaja() {
        return baja;
    }
    public void setBaja(LocalDateTime baja) {
        this.baja = baja;
    }
    
}
