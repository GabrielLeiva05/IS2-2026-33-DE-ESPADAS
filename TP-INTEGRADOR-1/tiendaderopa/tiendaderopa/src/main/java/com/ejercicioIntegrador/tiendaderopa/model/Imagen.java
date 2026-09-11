package com.ejercicioIntegrador.tiendaderopa.model;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoImagen;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//import org.hibernate.envers.Audited;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Imagen {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column()
    private String mime;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] contenido;

    private boolean eliminado;

    @Enumerated(EnumType.STRING)
    private TipoImagen tipoImagen;

    // Como id es String, generamos un UUID para cada nueva imagen antes de persistirla en la base de datos
    @PrePersist 
    public void generarId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

}
