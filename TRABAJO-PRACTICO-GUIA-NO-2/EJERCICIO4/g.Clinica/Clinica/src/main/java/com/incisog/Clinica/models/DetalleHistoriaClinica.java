package com.incisog.Clinica.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class DetalleHistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // El diagrama dice Date. Usamos LocalDate (la API de fechas moderna de Java):
    // representa solo la fecha, sin hora ni zona horaria, y en JSON viaja como "2026-10-13".
    private LocalDate fechaHistoria;

    // TEXT: el texto de una atención puede superar los 255 caracteres de un VARCHAR común.
    @Column(columnDefinition = "TEXT")
    private String detalleHistoria;

    private boolean eliminado = false;

    // Diagrama: DetalleHistoriaClinica *..1 Medico (quién atendió)
    @ManyToOne
    private Medico medico;

    // No hay referencia a HistoriaClinica: la relación la maneja la historia (composición).

    public DetalleHistoriaClinica() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getFechaHistoria() { return fechaHistoria; }
    public void setFechaHistoria(LocalDate fechaHistoria) { this.fechaHistoria = fechaHistoria; }

    public String getDetalleHistoria() { return detalleHistoria; }
    public void setDetalleHistoria(String detalleHistoria) { this.detalleHistoria = detalleHistoria; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }


}

