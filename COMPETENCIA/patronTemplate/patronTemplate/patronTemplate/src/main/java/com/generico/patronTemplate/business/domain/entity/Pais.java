package com.generico.patronTemplate.business.domain.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Pais extends BaseEntity<Long> {

    private String nombre;
   
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
	public String getNombre() { return nombre;}
	public void setNombre(String nombre) { this.nombre = nombre;}
    
}
