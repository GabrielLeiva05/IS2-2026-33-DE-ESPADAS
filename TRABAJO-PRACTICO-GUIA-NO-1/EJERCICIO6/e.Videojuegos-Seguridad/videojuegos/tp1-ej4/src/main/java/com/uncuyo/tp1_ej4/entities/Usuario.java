package com.uncuyo.tp1_ej4.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Usuario del sistema (login). La contrasena SIEMPRE se guarda encriptada
 * con BCrypt (ver PasswordEncoder en SecurityConfig) - nunca en texto plano.
 * No se marca con @Audited a proposito: no tiene sentido dejar historial de
 * hashes de contrasenas en la tabla de auditoria de Envers.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty(message = "El nombre de usuario es requerido")
    @Column(unique = true, nullable = false)
    private String username;

    // Hash BCrypt de la contrasena (nunca texto plano)
    @NotEmpty(message = "La contrasena es requerida")
    private String password;

    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "fk_usuario"),
            inverseJoinColumns = @JoinColumn(name = "fk_rol")
    )
    private Set<Rol> roles = new HashSet<>();
}
