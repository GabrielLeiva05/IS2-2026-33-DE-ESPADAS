package com.ejercicioIntegrador.tiendaderopa.model;


import com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements Serializable{


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private String id;

    @Column(nullable= false, length = 100)
    private String nombreUsuario; //Corresponde al correo

    private String clave;

    @Column
    private boolean eliminado = false;

    @OneToOne(optional = false) // No puede existir sin una Persona
    @JoinColumn(name = "persona_id", nullable = false, unique = true)
    private Persona persona;
    @Column
    @Enumerated(EnumType.STRING)
    private RolUsuario rolUsuario;

    public Usuario(String nombreUsuario, String clave, RolUsuario rolUsuario) {

        this.nombreUsuario = nombreUsuario;
        this.clave = clave;
        this.rolUsuario = rolUsuario;
    }

}
