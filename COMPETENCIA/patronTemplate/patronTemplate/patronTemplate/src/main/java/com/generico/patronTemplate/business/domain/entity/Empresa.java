package com.generico.patronTemplate.business.domain.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Empresa extends BaseEntity<Long> {

    private String razonSocial;
    private String cuit;
    private String correoElectronico;
    private String telefono;
    
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    
    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }
    
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    
    
}
