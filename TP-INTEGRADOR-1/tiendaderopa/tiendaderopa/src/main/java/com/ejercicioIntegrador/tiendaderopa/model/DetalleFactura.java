package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.web.WebProperties;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "detalle_factura")
public class DetalleFactura {
    @Id
    @GeneratedValue( strategy = GenerationType.UUID)
    private String id;

    @NotNull
    private int cantidad;

    private double subtotal;

    @NotNull(message = "El campo eliminado no puede ser nulo")
    private boolean eliminado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_factura", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_producto", nullable = false)
    private Producto producto;
}
