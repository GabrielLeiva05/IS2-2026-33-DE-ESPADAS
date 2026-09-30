package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "configuracion_correo_empresa")
public class ConfiguracionCorreoEmpresa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String clave;

    @Column(nullable = false)
    private String puerto;

    @Column(nullable = false)
    private String smtp;

    @Column(nullable = false)
    private boolean tls;

    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    public ConfiguracionCorreoEmpresa(String correo, String clave, String puerto, String smtp, boolean tls, Empresa empresa) {
        this.correo = correo;
        this.clave = clave;
        this.puerto = puerto;
        this.smtp = smtp;
        this.tls = tls;
        this.empresa = empresa;
    }
}