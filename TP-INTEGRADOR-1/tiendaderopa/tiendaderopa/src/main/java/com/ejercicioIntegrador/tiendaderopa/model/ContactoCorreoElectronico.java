package com.ejercicioIntegrador.tiendaderopa.model;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "contacto_correo_electronico")
public class ContactoCorreoElectronico extends Contacto {

    @Column(nullable = false)
    private String email;

    public ContactoCorreoElectronico(String email, TipoContacto tipoContacto, String observacion, Persona persona) {
        super(tipoContacto, observacion, persona);
        this.email = email;
    }
}
