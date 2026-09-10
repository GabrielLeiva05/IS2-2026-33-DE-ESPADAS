package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "Localidad")
@Getter
@Setter
@NoArgsConstructor
public class Localidad implements Serializable {
    
    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean eliminado = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;
    
    @OneToMany(mappedBy = "localidad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Direccion> direcciones = new ArrayList<>();

    
    @Column(nullable = false)
    private String codigoPostal;

    public Localidad(String nombre, String codigoPostal) {
        this.nombre = nombre;
        this.codigoPostal = codigoPostal;
    }


}
