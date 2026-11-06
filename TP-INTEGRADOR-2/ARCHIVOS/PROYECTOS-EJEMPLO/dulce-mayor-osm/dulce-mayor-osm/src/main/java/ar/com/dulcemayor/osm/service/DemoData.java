package ar.com.dulcemayor.osm.service;

import ar.com.dulcemayor.osm.model.PuntoVenta;
import ar.com.dulcemayor.osm.model.Sucursal;
import ar.com.dulcemayor.osm.repo.PuntoVentaRepository;
import ar.com.dulcemayor.osm.repo.SucursalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Datos de ejemplo en Gran Mendoza (coordenadas aproximadas). */
@Component
public class DemoData implements CommandLineRunner {

    private final PuntoVentaRepository puntos;
    private final SucursalRepository sucursales;

    public DemoData(PuntoVentaRepository puntos, SucursalRepository sucursales) {
        this.puntos = puntos;
        this.sucursales = sucursales;
    }

    @Override
    public void run(String... args) { cargar(); }

    public void cargar() {
        puntos.saveAll(List.of(
            // visitado=false -> pendientes para el PREVENTOR
            new PuntoVenta("Kiosco El Sol", "Av. San Martin 1200, Ciudad", "elsol@example.com", -32.8950, -68.8420, false),
            new PuntoVenta("Almacen Don Pepe", "Av. San Martin 300, Godoy Cruz", "donpepe@example.com", -32.9253, -68.8346, false),
            new PuntoVenta("Kiosco La Esquina", "Av. San Martin 800, Las Heras", "laesquina@example.com", -32.8510, -68.8280, false),
            new PuntoVenta("Maxikiosco Bandera", "Bandera de los Andes 1500, Guaymallen", "bandera@example.com", -32.8890, -68.7870, false),
            new PuntoVenta("Almacen Maipu", "Centro, Maipu", "almaipu@example.com", -32.9770, -68.7890, false),
            new PuntoVenta("Kiosco Lujan", "Centro, Lujan de Cuyo", "lujan@example.com", -33.0350, -68.8780, false),
            // visitado=true -> con pedido tomado, pendientes para el DESPACHO
            new PuntoVenta("Kiosco Dorrego", "Dorrego, Guaymallen", "dorrego@example.com", -32.9040, -68.8120, true),
            new PuntoVenta("Almacen Villa Nueva", "Villa Nueva, Guaymallen", "villanueva@example.com", -32.9000, -68.7600, true),
            new PuntoVenta("Kiosco Chacras", "Chacras de Coria, Lujan de Cuyo", "chacras@example.com", -32.9760, -68.8650, true),
            new PuntoVenta("Kiosco Godoy Cruz Sur", "Godoy Cruz", "gcsur@example.com", -32.9170, -68.8590, true)
        ));
        sucursales.saveAll(List.of(
            new Sucursal("Casa Central", "Acceso Este, Guaymallen", -32.8930, -68.7950, "https://picsum.photos/seed/dm1/260/130"),
            new Sucursal("Sucursal Godoy Cruz", "Godoy Cruz, Mendoza", -32.9300, -68.8700, "https://picsum.photos/seed/dm2/260/130")
        ));
    }

    public void resetear() {
        puntos.deleteAll();
        sucursales.deleteAll();
        cargar();
    }
}
