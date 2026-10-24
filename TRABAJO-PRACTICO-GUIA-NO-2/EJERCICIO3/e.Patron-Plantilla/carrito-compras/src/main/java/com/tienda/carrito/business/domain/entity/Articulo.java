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
 * Diagrama: Articulo (id, nombre, precio, eliminado).
 *   Articulo --> Imagen    (1...*): un articulo tiene una o varias imagenes (OneToMany unidireccional).
 *   Articulo --> Proveedor (1...*): cada articulo pertenece a un proveedor.
 */
@Entity
@Table(name = "articulos")
@AllArgsConstructor
@NoArgsConstructor
public class Articulo extends BaseEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "precio", nullable = false)
    private double precio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private List<Imagen> imagenes = new ArrayList<>();

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public List<Imagen> getImagenes() { return imagenes; }
    public void setImagenes(List<Imagen> imagenes) { this.imagenes = imagenes; }

    /** Imagenes que no fueron dadas de baja logica. */
    public List<Imagen> getImagenesActivas() {
        return imagenes.stream().filter(i -> !Boolean.TRUE.equals(i.isEliminado())).toList();
    }
}
