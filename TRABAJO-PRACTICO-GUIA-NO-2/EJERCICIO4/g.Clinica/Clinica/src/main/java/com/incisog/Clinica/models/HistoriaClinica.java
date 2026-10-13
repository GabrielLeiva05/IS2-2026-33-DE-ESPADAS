package com.incisog.Clinica.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.List;

@Entity
public class HistoriaClinica extends BaseEntity<String> implements Serializable {

    @OneToMany
    private List<DetalleHistoriaClinica> detalleHistoriaClinica;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }

}
