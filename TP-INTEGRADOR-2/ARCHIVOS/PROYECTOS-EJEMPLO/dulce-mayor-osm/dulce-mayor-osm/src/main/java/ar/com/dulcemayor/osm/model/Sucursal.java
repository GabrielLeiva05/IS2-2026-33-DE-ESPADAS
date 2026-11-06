package ar.com.dulcemayor.osm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sucursal de Dulce Mayor (sitio institucional). */
@Entity
@Getter @Setter @NoArgsConstructor
public class Sucursal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    private double lat;
    private double lng;
    private String fotoUrl;

    public Sucursal(String nombre, String direccion, double lat, double lng, String fotoUrl) {
        this.nombre = nombre; this.direccion = direccion; this.lat = lat; this.lng = lng; this.fotoUrl = fotoUrl;
    }
}
