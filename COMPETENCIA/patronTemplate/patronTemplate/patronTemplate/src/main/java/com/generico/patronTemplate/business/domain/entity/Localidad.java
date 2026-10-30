package com.generico.patronTemplate.business.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Localidad extends BaseEntity<Long> {

	private String nombre;
	private String codigoPostal;
	@ManyToOne
	private Departamento departamento;
    
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
	
    public String getCodigoPostal() {return codigoPostal;}
	public void setCodigoPostal(String codigoPostal) {this.codigoPostal = codigoPostal;}
	
	public Departamento getDepartamento() {return departamento;}
	public void setDepartamento(Departamento departamento) {this.departamento = departamento;}
    
    
}
