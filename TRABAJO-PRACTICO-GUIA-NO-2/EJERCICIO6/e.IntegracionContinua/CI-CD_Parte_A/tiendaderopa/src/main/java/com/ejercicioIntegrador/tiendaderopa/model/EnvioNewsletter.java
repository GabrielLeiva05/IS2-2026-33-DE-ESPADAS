package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "envios_newsletter")
@Getter
@Setter
@NoArgsConstructor
public class EnvioNewsletter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(nullable = false)
    private int cantidadProductos;

    @Column(nullable = false)
    private int destinatariosEnviados;

    @Column(nullable = false)
    private int destinatariosFallidos;

    public EnvioNewsletter(LocalDateTime fechaEnvio, int cantidadProductos,
                           int destinatariosEnviados, int destinatariosFallidos) {
        this.fechaEnvio = fechaEnvio;
        this.cantidadProductos = cantidadProductos;
        this.destinatariosEnviados = destinatariosEnviados;
        this.destinatariosFallidos = destinatariosFallidos;
    }
}
