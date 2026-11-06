package ar.com.dulcemayor.osm.service;

import ar.com.dulcemayor.osm.model.PuntoVenta;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Logica de ruteo:
 *  1) Orden de visita: heuristica del vecino mas cercano (Haversine).
 *  2) Trazado por calles reales: OSRM (datos de OpenStreetMap), con fallback a linea recta.
 */
@Service
public class RutaService {

    private static final Logger log = LoggerFactory.getLogger(RutaService.class);

    /** geometria = lista de [lat,lng] lista para Leaflet. */
    public record Trayecto(List<double[]> geometria, double km, double minutos, String fuente) { }

    private final RestClient http;
    private final String osrmUrl;

    public RutaService(RestClient.Builder builder, @Value("${app.osrm.url}") String osrmUrl) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(3000);
        f.setReadTimeout(6000);
        this.http = builder.requestFactory(f).build();
        this.osrmUrl = osrmUrl;
    }

    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1), dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }

    /** Punto mas cercano al origen. */
    public PuntoVenta masCercano(double lat, double lng, List<PuntoVenta> candidatos) {
        return candidatos.stream()
                .min(Comparator.comparingDouble(p -> haversineKm(lat, lng, p.getLat(), p.getLng())))
                .orElse(null);
    }

    /** Ordena los puntos con vecino mas cercano partiendo de (lat,lng). */
    public List<PuntoVenta> ordenar(double lat, double lng, List<PuntoVenta> puntos) {
        List<PuntoVenta> pendientes = new ArrayList<>(puntos);
        List<PuntoVenta> orden = new ArrayList<>();
        double cLat = lat, cLng = lng;
        while (!pendientes.isEmpty()) {
            PuntoVenta sig = masCercano(cLat, cLng, pendientes);
            orden.add(sig);
            pendientes.remove(sig);
            cLat = sig.getLat();
            cLng = sig.getLng();
        }
        return orden;
    }

    /** Trazado por calles pasando por los puntos [lat,lng] en el orden dado. */
    public Trayecto trayecto(List<double[]> puntos) {
        if (puntos.size() < 2) return new Trayecto(List.of(), 0, 0, "vacio");
        try {
            String coords = puntos.stream().map(p -> p[1] + "," + p[0]).collect(Collectors.joining(";"));
            URI uri = URI.create(osrmUrl + "/route/v1/driving/" + coords + "?overview=full&geometries=geojson");
            JsonNode root = http.get().uri(uri).retrieve().body(JsonNode.class);
            JsonNode route = root == null ? null : root.path("routes").path(0);
            if (route == null || route.isMissingNode()) throw new IllegalStateException("OSRM sin rutas");
            List<double[]> geo = new ArrayList<>();
            for (JsonNode c : route.path("geometry").path("coordinates")) {
                geo.add(new double[]{c.get(1).asDouble(), c.get(0).asDouble()});
            }
            return new Trayecto(geo, route.path("distance").asDouble() / 1000.0,
                    route.path("duration").asDouble() / 60.0, "calles (OSRM / OpenStreetMap)");
        } catch (Exception e) {
            log.warn("OSRM no disponible, se usa linea recta: {}", e.getMessage());
            double km = 0;
            for (int i = 1; i < puntos.size(); i++) {
                km += haversineKm(puntos.get(i - 1)[0], puntos.get(i - 1)[1], puntos.get(i)[0], puntos.get(i)[1]);
            }
            return new Trayecto(new ArrayList<>(puntos), km, km / 30.0 * 60.0, "linea recta (fallback)");
        }
    }
}
