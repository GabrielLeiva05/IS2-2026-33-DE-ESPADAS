package com.tienda.carrito.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Diagrama: Usuario (id, nombre, clave, eliminado). Se agrega "rol" para separar administradores de clientes.
 * La clave se guarda siempre con hash BCrypt.
 */
@Entity
@Table(name = "usuarios")
@AllArgsConstructor
@NoArgsConstructor
public class Usuario extends BaseEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "clave", nullable = false, length = 100)
    private String clave;

    // El DEFAULT permite que "ddl-auto=update" agregue la columna a una base ya existente (los usuarios viejos quedan como CLIENTE)
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'CLIENTE'")
    private Rol rol;

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
