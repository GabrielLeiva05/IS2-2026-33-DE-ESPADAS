package com.ejercicioIntegrador.tiendaderopa.model;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

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
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    /*
    @OneToOne
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;
    */

    public Contacto(TipoContacto tipoContacto, String observacion, Persona persona) {
        this.tipoContacto = tipoContacto;
        this.observacion = observacion;
        this.persona = persona;
    }
}
