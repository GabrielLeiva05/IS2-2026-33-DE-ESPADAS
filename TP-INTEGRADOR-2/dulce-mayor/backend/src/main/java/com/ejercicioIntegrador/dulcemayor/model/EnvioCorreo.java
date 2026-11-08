package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoEnvio;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoCorreo;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro auditable de cada correo que el sistema intenta enviar. Sirve para probar las
 * notificaciones sin un proveedor SMTP y para diagnosticar fallos de entrega.
 */
@Entity
@Table(name = "envios_correo")
@Getter
@Setter
@NoArgsConstructor
public class EnvioCorreo extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String destinatario;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCorreo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvio estado;

    @Column(length = 500)
    private String detalle;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void inicializar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
