package com.competencia.blockazo.model.entity;

import com.competencia.blockazo.model.enums.Proveedor;
import com.competencia.blockazo.model.enums.Rol;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Persona / usuario del sistema.
 * El CORREO es el nombre de usuario (username) para el login.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity<Long> {

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    // Puede quedar vacio para quienes ingresan con Google hasta que completen su perfil
    @Column(unique = true, length = 10)
    private String dni;

    private LocalDate fechaNacimiento;

    @Column(length = 200)
    private String direccion;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    // Solo se recibe en el JSON (alta / modificacion), NUNCA se devuelve.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Proveedor proveedor;

    private LocalDateTime fechaAlta;

    // ---- Implementacion de los metodos abstractos de BaseEntity ----
    @Override
    public Long getId() { return id; }

    @Override
    public void setId(Long id) { this.id = id; }

    @Override
    @JsonIgnore
    public Boolean isEliminado() { return eliminado; }

    @Override
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }

    /** Un usuario que entro con Google puede no tener todavia DNI, fecha de nacimiento o direccion. */
    @JsonProperty(value = "perfilCompleto", access = JsonProperty.Access.READ_ONLY)
    public boolean isPerfilCompleto() {
        return dni != null && !dni.isBlank()
                && fechaNacimiento != null
                && direccion != null && !direccion.isBlank();
    }

    /** Indica si el usuario tiene una clave local (los de Google arrancan sin clave). */
    @JsonProperty(value = "tieneClave", access = JsonProperty.Access.READ_ONLY)
    public boolean isTieneClave() {
        return clave != null && !clave.isBlank();
    }
}
