package com.ejercicioIntegrador.tiendaderopa.model;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "contacto_telefonico")
public class ContactoTelefonico extends Contacto {

    @Column(nullable = false)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTelefono tipoTelefono;

    public ContactoTelefonico(String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion, Persona persona) {
        super(tipoContacto, observacion, persona);
        this.telefono = telefono;
        this.tipoTelefono = tipoTelefono;
    }
}
