package com.ejercicioIntegrador.tiendaderopa.model;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "contacto")
// @Inheritance sirve para establecer como se estructuran las
// tablas de la jerarquía de clases en la base de datos.
// Con JOINED se crea una tabla para la clase base y una tabla para cada subclase.
// Solo que las de la subclase comparten la misma PK que la tabla de la clase base,
// y se relacionan con ella mediante una FK. Pero guardan solo los atributos propios de la subclase.
@Inheritance(strategy = InheritanceType.JOINED)
public class Contacto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private String id;

    /* @Enumerated(EnumType.STRING) sirve para mapear un tipo de enumerador (enum) 
    de Java en una columna de una base de datos.
    indica que el valor del enum se guardará como un String en la base de datos.
    */
    @Enumerated(EnumType.STRING)
    @Column
    private TipoContacto tipoContacto;

    @Column
    private String observacion;

    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_proveedor") // nullable a propósito
    private Proveedor proveedor;

    public Contacto(TipoContacto tipoContacto, String observacion, Persona persona) {
        this.tipoContacto = tipoContacto;
        this.observacion = observacion;
        this.persona = persona;
    }
    public Contacto(TipoContacto tipoContacto, String observacion, Proveedor proveedor) {
        this.tipoContacto = tipoContacto;
        this.observacion = observacion;
        this.proveedor = proveedor;
    }
}
