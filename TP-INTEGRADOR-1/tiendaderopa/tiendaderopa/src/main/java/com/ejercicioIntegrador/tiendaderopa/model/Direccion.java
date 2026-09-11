package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Setter
@Table(name = "Localidad")
@NoArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    public Direccion(String calle, String numeracion, String barrio, String manzanaPiso, String casaDepartamento, String referencia, Localidad localidad) {
        this.calle = calle;
        this.numeracion = numeracion;
        this.barrio = barrio;
        this.manzanaPiso = manzanaPiso;
        this.casaDepartamento = casaDepartamento;
        this.referencia = referencia;
        this.localidad = localidad;
    }

}
