package com.tienda.carrito.business.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Diagrama: Carrito (id, total, eliminado).
 *   Carrito --> Usuario  : cada carrito pertenece a un usuario.
 *   Carrito <>-- Detalle : COMPOSICION (cascade ALL).
 * Un carrito con eliminado = true es un carrito CERRADO (compra confirmada).
 */
@Entity
@Table(name = "carritos")
@AllArgsConstructor
@NoArgsConstructor
public class Carrito extends BaseEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "total", nullable = false)
    private double total;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Detalle> detalles = new ArrayList<>();

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public List<Detalle> getDetalles() { return detalles; }
    public void setDetalles(List<Detalle> detalles) { this.detalles = detalles; }

    /** Detalles que no fueron quitados del carrito. */
    public List<Detalle> getDetallesActivos() {
        return detalles.stream().filter(d -> !Boolean.TRUE.equals(d.isEliminado())).toList();
    }
}
