package com.is2.tinder.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String nombre;
    private String apellido;
    private String mail;
    private String clave;

    @ManyToOne
    private Zona zona;

    private LocalDateTime alta;
    private LocalDateTime baja;

    @OneToOne
    private Foto foto;

    public Foto getFoto() {
        return foto;
    }
    public void setFoto(Foto foto) {
        this.foto = foto;
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
    public String getApellido() {
        return apellido;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }
    public String getClave() {
        return clave;
    }
    public void setClave(String clave) {
        this.clave = clave;
    }
    public Zona getZona() {
        return zona;
    }
    public void setZona(Zona zona) {
        this.zona = zona;
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
