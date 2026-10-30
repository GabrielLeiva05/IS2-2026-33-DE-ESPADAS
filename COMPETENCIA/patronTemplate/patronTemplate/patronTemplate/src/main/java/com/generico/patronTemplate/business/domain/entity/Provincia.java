package com.generico.patronTemplate.business.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Provincia extends BaseEntity<Long> {

	private String nombre;
	@ManyToOne
	private Pais pais;
    
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    
    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }
}
