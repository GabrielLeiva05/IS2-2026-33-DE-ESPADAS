package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//import org.hibernate.envers.Audited;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Imagen {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column()
    private String mime;
    @Lob @Basic(fetch = FetchType.LAZY)
    private byte[] contenido;
    private boolean eliminado;

    @Enumerated(EnumType.STRING)
    private TipoImagen tipoImagen;

}
