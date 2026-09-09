package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "Localidad")
public class Direccion implements Serializable {
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;
    
    @Column(nullable = false)
    private boolean eliminado = false;
    
    @Column(nullable = false)
    private String calle;
    @Column(nullable = false)
    private String numeracion;
    @Column
    private String barrio;
    @Column
    private String manzanaPiso;
    @Column
    private String casaDepartamento;
    @Column
    private String referencia;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;
    
    public Direccion() {
    }

    public Direccion(String calle, String numeracion, String barrio, String manzanaPiso, String casaDepartamento, String referencia, Localidad localidad) {
        this.calle = calle;
        this.numeracion = numeracion;
        this.barrio = barrio;
        this.manzanaPiso = manzanaPiso;
        this.casaDepartamento = casaDepartamento;
        this.referencia = referencia;
        this.localidad = localidad;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

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

    public String getManzanaPiso() {
        return manzanaPiso;
    }

    public void setManzanaPiso(String manzanaPiso) {
        this.manzanaPiso = manzanaPiso;
    }

    public String getCasaDepartamento() {
        return casaDepartamento;
    }

    public void setCasaDepartamento(String casaDepartamento) {
        this.casaDepartamento = casaDepartamento;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Localidad getLocalidad() {
        return localidad;
    }

    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }
    
    
    
    
    

}
