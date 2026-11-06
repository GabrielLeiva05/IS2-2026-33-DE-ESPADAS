package ar.com.dulcemayor.osm.web;

import ar.com.dulcemayor.osm.model.PuntoVenta;
import ar.com.dulcemayor.osm.model.Sucursal;
import ar.com.dulcemayor.osm.repo.PuntoVentaRepository;
import ar.com.dulcemayor.osm.repo.SucursalRepository;
import ar.com.dulcemayor.osm.service.DemoData;
import ar.com.dulcemayor.osm.service.NotificacionService;
import ar.com.dulcemayor.osm.service.RutaService;
import ar.com.dulcemayor.osm.service.RutaService.Trayecto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@RestController
@RequestMapping("/api")
public class ApiController {

    public record RutaDto(List<PuntoVenta> orden, List<double[]> geometria, double km, double minutos, String fuente) { }
    public record CompletarDto(PuntoVenta completado, PuntoVenta siguiente, String mensaje) { }

    private final PuntoVentaRepository puntos;
    private final SucursalRepository sucursales;
    private final RutaService rutas;
    private final NotificacionService notificaciones;
    private final DemoData demo;

    public ApiController(PuntoVentaRepository puntos, SucursalRepository sucursales, RutaService rutas,
                         NotificacionService notificaciones, DemoData demo) {
        this.puntos = puntos; this.sucursales = sucursales; this.rutas = rutas;
        this.notificaciones = notificaciones; this.demo = demo;
    }

    /** preventor: puntos sin pedido tomado. despacho: con pedido tomado y sin entregar. */
    private Predicate<PuntoVenta> filtro(String modo) {
        return switch (modo) {
            case "preventor" -> p -> !p.isVisitado();
            case "despacho" -> p -> p.isVisitado() && !p.isEntregado();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modo invalido: " + modo);
        };
    }

    @GetMapping("/puntos")
    public List<PuntoVenta> listarPuntos() { return puntos.findAll(); }

    /** Alta de punto de venta por el preventor (lat/lng vienen del mapa o de Nominatim). */
    @PostMapping("/puntos")
    @ResponseStatus(HttpStatus.CREATED)
    public PuntoVenta alta(@Valid @RequestBody PuntoVenta p) {
        p.setId(null);
        p.setVisitado(false);
        p.setEntregado(false);
        return puntos.save(p);
    }

    @GetMapping("/sucursales")
    public List<Sucursal> sucursales() { return sucursales.findAll(); }

    /** Ruta optima (vecino mas cercano) desde la posicion actual + trazado por calles. */
    @GetMapping("/ruta/{modo}")
    public RutaDto ruta(@PathVariable String modo, @RequestParam double lat, @RequestParam double lng) {
        List<PuntoVenta> pendientes = puntos.findAll().stream().filter(filtro(modo)).toList();
        List<PuntoVenta> orden = rutas.ordenar(lat, lng, pendientes);
        List<double[]> coords = new ArrayList<>();
        coords.add(new double[]{lat, lng});
        orden.forEach(p -> coords.add(new double[]{p.getLat(), p.getLng()}));
        Trayecto t = rutas.trayecto(coords);
        return new RutaDto(orden, t.geometria(), t.km(), t.minutos(), t.fuente());
    }

    /** Marca el punto como atendido/entregado y propone el siguiente. */
    @PostMapping("/ruta/{modo}/completar/{id}")
    public CompletarDto completar(@PathVariable String modo, @PathVariable Long id) {
        Predicate<PuntoVenta> filtro = filtro(modo);
        PuntoVenta actual = puntos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Punto inexistente"));
        if ("preventor".equals(modo)) actual.setVisitado(true); else actual.setEntregado(true);
        puntos.save(actual);

        List<PuntoVenta> restantes = puntos.findAll().stream().filter(filtro).toList();
        PuntoVenta siguiente = rutas.masCercano(actual.getLat(), actual.getLng(), restantes);

        String msg = siguiente == null ? "Ruta finalizada" : "Siguiente: " + siguiente.getNombre();
        if ("despacho".equals(modo) && siguiente != null) {
            msg += " | " + notificaciones.avisarProximaEntrega(siguiente);
        }
        return new CompletarDto(actual, siguiente, msg);
    }

    @PostMapping("/demo/reset")
    public void reset() { demo.resetear(); }
}
