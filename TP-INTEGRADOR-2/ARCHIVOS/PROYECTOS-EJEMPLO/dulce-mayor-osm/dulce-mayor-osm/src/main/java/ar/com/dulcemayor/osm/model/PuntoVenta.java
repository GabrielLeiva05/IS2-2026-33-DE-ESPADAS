package ar.com.dulcemayor.osm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Punto de venta (kiosco, almacen...) con geolocalizacion. */
@Entity
@Getter @Setter @NoArgsConstructor
public class PuntoVenta {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;
    private String direccion;
    private String email;

    @NotNull private Double lat;
    @NotNull private Double lng;

    /** El preventor ya tomo el pedido en este punto. */
    private boolean visitado;
    /** El equipo de despacho ya entrego el pedido. */
    private boolean entregado;

    public PuntoVenta(String nombre, String direccion, String email, double lat, double lng, boolean visitado) {
        this.nombre = nombre; this.direccion = direccion; this.email = email;
        this.lat = lat; this.lng = lng; this.visitado = visitado;
    }
}
