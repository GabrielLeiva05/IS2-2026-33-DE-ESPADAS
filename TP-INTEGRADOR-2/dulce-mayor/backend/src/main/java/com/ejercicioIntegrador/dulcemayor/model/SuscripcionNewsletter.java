package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Suscripción al newsletter mensual (opt-in). La baja se hace con el token, sin necesidad de
 * cuenta, y se respeta en la misma ejecución en la que se pide.
 */
@Entity
@Table(name = "newsletter_suscripciones",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
public class SuscripcionNewsletter extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(nullable = false, unique = true, length = 40)
    private String tokenBaja;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    private LocalDateTime fechaBaja;

    private LocalDateTime fechaUltimoEnvio;

    @PrePersist
    void inicializar() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
        if (tokenBaja == null) {
            tokenBaja = UUID.randomUUID().toString();
        }
    }
}
