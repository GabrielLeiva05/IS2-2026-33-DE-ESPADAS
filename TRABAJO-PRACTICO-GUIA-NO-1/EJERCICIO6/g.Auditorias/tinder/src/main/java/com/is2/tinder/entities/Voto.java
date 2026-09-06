package com.is2.tinder.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime fecha;
    private LocalDateTime respuesta;

    @ManyToOne
    private Mascota mascota1;
    @ManyToOne
    private Mascota mascota2;

    public Mascota getMascota1() {
        return mascota1;
    }
    public void setMascota1(Mascota mascota1) {
        this.mascota1 = mascota1;
    }
    public Mascota getMascota2() {
        return mascota2;
    }
    public void setMascota2(Mascota mascota2) {
        this.mascota2 = mascota2;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public LocalDateTime getFecha() {
        return fecha;
    }
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    public LocalDateTime getRespuesta() {
        return respuesta;
    }
    public void setRespuesta(LocalDateTime respuesta) {
        this.respuesta = respuesta;
    }
    
}
