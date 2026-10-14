package com.incisog.Clinica.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class HistoriaClinica extends BaseEntity{
    // Diagrama: HistoriaClinica *..1 Paciente
    @ManyToOne
    private Paciente paciente;

    // Diagrama: HistoriaClinica *..1 Usuario (quien creó / gestiona la historia)
    @ManyToOne
    private Usuario usuario;

    // Diagrama: composición (rombo negro) con DetalleHistoriaClinica.
    // cascade = ALL + orphanRemoval = los detalles viven y mueren con la historia.
    // @JoinColumn: la FK "historia_clinica_id" queda en la tabla del detalle
    // (sin esto JPA crearía una tabla intermedia).
    // @JsonIgnore: los detalles se piden con GET /api/historias/{id}/detalles,
    // que devuelve solo los activos y ordenados por fecha.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "historia_clinica_id")
    @JsonIgnore
    private List<DetalleHistoriaClinica> detalles = new ArrayList<>();

    public HistoriaClinica() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<DetalleHistoriaClinica> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleHistoriaClinica> detalles) { this.detalles = detalles; }

}
