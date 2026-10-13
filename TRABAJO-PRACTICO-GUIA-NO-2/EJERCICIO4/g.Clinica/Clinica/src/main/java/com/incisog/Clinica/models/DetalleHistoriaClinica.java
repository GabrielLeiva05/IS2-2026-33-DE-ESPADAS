package com.incisog.Clinica.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class DetalleHistoriaClinica extends BaseEntity<String> {

    @ManyToOne
    private HistoriaClinica historiaClinica;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }


}

