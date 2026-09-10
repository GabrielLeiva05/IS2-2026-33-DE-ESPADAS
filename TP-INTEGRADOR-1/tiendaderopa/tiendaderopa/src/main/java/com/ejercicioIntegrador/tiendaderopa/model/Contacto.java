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
public class Contacto implements Serializable {

    @Id
    @Column(nullable = false)
    private String id;

    @Column
    private TipoContacto tipoContacto;

    @Column
    private String observacion;

    @Column
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    /*
    @OneToOne
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;
    */

    public Contacto(TipoContacto tipoContacto, String observacion) {
        this.tipoContacto = tipoContacto;
        this.observacion = observacion;
    }
}
