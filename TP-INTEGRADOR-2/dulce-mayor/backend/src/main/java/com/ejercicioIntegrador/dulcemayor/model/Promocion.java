package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Promoción comercial (HU-22). Herencia JOINED, como Factura: los datos comunes en "promociones" y los propios de
 * cada tipo en su tabla.
 *
 * Alcance: los productos y/o subcategorías ("grupos de productos" de la estructura actual) a los que se aplica.
 * Para PromocionPorcentual y PromocionVolumen es obligatorio; en CodigoDescuento, vacío significa "todo el pedido".
 *
 * Los pedidos guardan una copia de cada descuento aplicado (DescuentoPedido): modificar o dar de baja la promoción
 * después no altera los pedidos ya hechos.
 */
@Entity
@Table(name = "promociones")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Promocion extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    /** Desactivar sin eliminar: deja de aplicarse pero se sigue viendo en el listado. */
    @Column(nullable = false)
    private boolean activa = true;

    @ManyToMany
    @JoinTable(name = "promocion_productos", joinColumns = @JoinColumn(name = "promocion_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id"))
    private Set<Producto> productos = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "promocion_subcategorias", joinColumns = @JoinColumn(name = "promocion_id"),
            inverseJoinColumns = @JoinColumn(name = "subcategoria_id"))
    private Set<SubCategoria> subCategorias = new LinkedHashSet<>();

    public abstract TipoPromocion getTipo();

    /** Activa, no eliminada y dentro de su período (inicio y fin inclusive). */
    public boolean vigenteEn(LocalDate fecha) {
        return activa && !isEliminado() && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    public boolean tieneAlcanceRestringido() {
        return !productos.isEmpty() || !subCategorias.isEmpty();
    }

    /** Si el producto está alcanzado (por sí mismo o por su subcategoría). Sin alcance restringido: todos. */
    public boolean alcanza(Producto producto) {
        if (!tieneAlcanceRestringido()) {
            return true;
        }
        if (productos.stream().anyMatch(p -> p.getId().equals(producto.getId()))) {
            return true;
        }
        SubCategoria sub = producto.getSubCategoria();
        return sub != null && subCategorias.stream().anyMatch(s -> s.getId().equals(sub.getId()));
    }
}
