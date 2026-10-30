package com.generico.patronTemplate.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Direccion extends BaseEntity<Long> {

	private String calle;
	private String numeracion;
    private String barrio;
	private String pisoCasa;
	private String puertaManzana;
	private String ubicacionCoordenadaX;
	private String ubicacionCoordenadaY;
	@Column(length = 500)
	private String observacion;
	@ManyToOne
	private Localidad localidad=new Localidad();
    
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }

    public String getCalle() {
		return calle;
	}
	public void setCalle(String calle) {
		this.calle = calle;
	}
	public String getNumeracion() {
		return numeracion;
	}
	public void setNumeracion(String numeracion) {
		this.numeracion = numeracion;
	}
	public String getBarrio() {
		return barrio;
	}
	public void setBarrio(String barrio) {
		this.barrio = barrio;
	}
	public String getPisoCasa() {
		return pisoCasa;
	}
	public void setPisoCasa(String pisoCasa) {
		this.pisoCasa = pisoCasa;
	}
	public String getPuertaManzana() {
		return puertaManzana;
	}
	public void setPuertaManzana(String puertaManzana) {
		this.puertaManzana = puertaManzana;
	}
	public String getUbicacionCoordenadaX() {
		return ubicacionCoordenadaX;
	}
	public void setUbicacionCoordenadaX(String ubicacionCoordenadaX) {
		this.ubicacionCoordenadaX = ubicacionCoordenadaX;
	}
	public String getUbicacionCoordenadaY() {
		return ubicacionCoordenadaY;
	}
	public void setUbicacionCoordenadaY(String ubicacionCoordenadaY) {
		this.ubicacionCoordenadaY = ubicacionCoordenadaY;
	}
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	public Localidad getLocalidad() {
		return localidad;
	}
	public void setLocalidad(Localidad localidad) {
		this.localidad = localidad;
	}
    
    
}
