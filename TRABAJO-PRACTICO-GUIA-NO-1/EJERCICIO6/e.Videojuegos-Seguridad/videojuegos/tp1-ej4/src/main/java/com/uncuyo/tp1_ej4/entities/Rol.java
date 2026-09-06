package com.uncuyo.tp1_ej4.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un rol de seguridad (ej: "ADMIN", "USER").
 * Spring Security antepone automaticamente el prefijo "ROLE_" cuando se usa
 * hasRole()/roles(), asi que aca se guarda el nombre SIN ese prefijo
 * (quedando ROLE_ADMIN, ROLE_USER como pide el enunciado).
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty(message = "El nombre del rol es requerido")
    @Column(unique = true, nullable = false, length = 20)
    private String nombre;
}
