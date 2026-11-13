package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuenta de acceso. nombreUsuario guarda el CORREO ELECTRÓNICO, que es el identificador de ingreso
 * (mismo criterio que el Integrador 1). La clave se guarda siempre con BCrypt y es nula en las
 * cuentas creadas con Google o Facebook. Nunca se expone en un DTO.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String nombreUsuario;

    @Column(length = 100)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Column(nullable = false)
    private boolean debeCambiarClave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProveedorAutenticacion proveedorAutenticacion;
}
