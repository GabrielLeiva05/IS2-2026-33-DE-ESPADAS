package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reutilizada del Integrador 1 (Direccion) con los atributos del diagrama nuevo.
 * Latitud y longitud son opcionales para las personas; los puntos de venta las exigirán.
 */
@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
public class Direccion extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String calle;

    @Column(nullable = false, length = 20)
    private String numeracion;

    @Column(length = 100)
    private String barrio;

    @Column(length = 50)
    private String manzanaPiso;

    @Column(length = 50)
    private String casaDepartamento;

    @Column(length = 255)
    private String referencia;

    private Double latitud;

    private Double longitud;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;
}
