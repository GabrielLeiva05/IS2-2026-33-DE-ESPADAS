package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.service.rutas.MatrizRecorrido;
import com.ejercicioIntegrador.dulcemayor.service.rutas.MotorRecorridos;
import com.ejercicioIntegrador.dulcemayor.dto.GeocodificacionDTO;
import com.ejercicioIntegrador.dulcemayor.dto.TrayectoDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.ServicioExternoNoDisponibleException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicios de mapas que OpenStreetMap NO resuelve por sí solo (el mapa en sí lo dibuja Leaflet en el navegador):
 *  - Geocodificación y geocodificación inversa con Nominatim (datos de OpenStreetMap).
 *  - Motor de recorridos (MotorRecorridos) con OSRM: matriz de tiempos de manejo entre puntos (/table) para que
 *    el servicio de rutas decida el orden, y trazado por calles en el orden decidido (/route). Si OSRM no responde,
 *    se usan distancias / líneas rectas y el campo "fuente" o el criterio lo aclaran: nunca se presenta una línea
 *    recta como recorrido por calles.
 * Se consultan desde el backend (y no desde el navegador) para respetar la política de uso de Nominatim:
 * como máximo una consulta por segundo, un User-Agent que identifique a la aplicación y resultados en caché.
 * No extiende BaseService porque no administra una entidad.
 */
@Service
public class ServicioMapas implements MotorRecorridos {

    private static final Logger LOG = LoggerFactory.getLogger(ServicioMapas.class);
    private static final long INTERVALO_MINIMO_MS = 1100;
    private static final int TAMANIO_CACHE = 300;
    private static final int MAXIMO_RESULTADOS = 5;
    /** Límite por defecto de OSRM para /table (--max-table-size). */
    private static final int MAXIMO_PUNTOS_MATRIZ = 100;

    private final RestClient nominatim;
    private final RestClient osrm;
    private final String pais;
    private final String urlOsrm;
    private final Map<String, Object> cache = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Object> mayor) {
            return size() > TAMANIO_CACHE;
        }
    };
    private long ultimaConsultaNominatim;

    public ServicioMapas(@Value("${app.mapas.nominatim-url}") String urlNominatim,
                         @Value("${app.mapas.osrm-url}") String urlOsrm,
                         @Value("${app.mapas.user-agent}") String userAgent,
                         @Value("${app.mapas.pais:ar}") String pais) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(4000);
        tiempos.setReadTimeout(8000);
        this.nominatim = RestClient.builder().baseUrl(urlNominatim).requestFactory(tiempos)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "es").build();
        this.osrm = RestClient.builder().requestFactory(tiempos)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent).build();
        this.pais = pais;
        this.urlOsrm = urlOsrm.endsWith("/") ? urlOsrm.substring(0, urlOsrm.length() - 1) : urlOsrm;
    }

    // ------------------------------------------------------------------
    // Geocodificación (dirección -> coordenadas)
    // ------------------------------------------------------------------

    /** Hasta 5 ubicaciones posibles para la dirección escrita. Lista vacía si no encontró ninguna confiable. */
    public List<GeocodificacionDTO> geocodificar(String direccion) throws MiException {
        String texto = direccion == null ? "" : direccion.trim();
        if (texto.length() < 4 || texto.length() > 200) {
            throw new MiException("Escribí la dirección con calle, número y localidad");
        }
        String clave = "dir:" + texto.toLowerCase(Locale.ROOT);
        Object enCache = leerCache(clave);
        if (enCache instanceof GeocodificacionDTO[] guardado) {
            return List.of(guardado);
        }
        ResultadoNominatim[] resultados = consultarNominatim(() -> nominatim.get()
                .uri(b -> b.path("/search").queryParam("format", "jsonv2").queryParam("addressdetails", 1)
                        .queryParam("limit", MAXIMO_RESULTADOS).queryParam("countrycodes", pais)
                        .queryParam("q", "{q}").build(texto))
                .retrieve().body(ResultadoNominatim[].class));
        List<GeocodificacionDTO> encontrados = new ArrayList<>();
        if (resultados != null) {
            for (ResultadoNominatim r : resultados) {
                convertir(r).ifPresent(encontrados::add);
            }
        }
        guardarCache(clave, encontrados.toArray(new GeocodificacionDTO[0]));
        return encontrados;
    }

    /** Dirección aproximada del punto tocado en el mapa. Vacío si Nominatim no encuentra una dirección. */
    public Optional<GeocodificacionDTO> geocodificarInversa(Double latitud, Double longitud) throws MiException {
        validarCoordenadas(latitud, longitud);
        String clave = String.format(Locale.ROOT, "inv:%.5f,%.5f", latitud, longitud);
        Object enCache = leerCache(clave);
        if (enCache instanceof GeocodificacionDTO guardado) {
            return Optional.of(guardado);
        }
        ResultadoNominatim resultado = consultarNominatim(() -> nominatim.get()
                .uri(b -> b.path("/reverse").queryParam("format", "jsonv2").queryParam("addressdetails", 1)
                        .queryParam("zoom", 18).queryParam("lat", latitud).queryParam("lon", longitud).build())
                .retrieve().body(ResultadoNominatim.class));
        // Con las coordenadas tocadas: la dirección es aproximada, pero el punto guardado es el elegido.
        Optional<GeocodificacionDTO> encontrado = Optional.ofNullable(resultado)
                .filter(r -> r.error() == null)
                .flatMap(this::convertir)
                .map(r -> new GeocodificacionDTO(latitud, longitud, r.descripcion(), r.calle(), r.numeracion(),
                        r.barrio(), r.localidad(), r.departamento(), r.provincia(), r.codigoPostal()));
        encontrado.ifPresent(r -> guardarCache(clave, r));
        return encontrado;
    }

    // ------------------------------------------------------------------
    // Trazado del recorrido (enrutamiento)
    // ------------------------------------------------------------------

    /**
     * Matriz de tiempos de manejo entre todos los puntos con el servicio /table de OSRM (una sola consulta).
     * Si OSRM no responde, devuelve algún tramo sin ruta o hay más puntos que su límite, se usa la distancia en
     * línea recta y el criterio lo informa.
     */
    @Override
    public MatrizRecorrido matriz(List<double[]> puntos) {
        if (puntos == null || puntos.size() < 2) {
            return new MatrizRecorrido(new double[puntos == null ? 0 : puntos.size()][puntos == null ? 0 : puntos.size()],
                    true, "sin tramos");
        }
        if (puntos.size() > MAXIMO_PUNTOS_MATRIZ) {
            LOG.warn("Ruta con {} puntos: supera el límite de /table de OSRM, se ordena en línea recta", puntos.size());
            return MatrizRecorrido.lineaRecta(puntos);
        }
        try {
            RespuestaTablaOsrm respuesta = osrm.get()
                    .uri(URI.create(urlOsrm + "/table/v1/driving/" + coordenadasOsrm(puntos) + "?annotations=duration"))
                    .retrieve().body(RespuestaTablaOsrm.class);
            if (respuesta == null || !"Ok".equals(respuesta.code()) || respuesta.durations() == null
                    || respuesta.durations().size() != puntos.size()) {
                throw new IllegalStateException("OSRM no devolvió la matriz");
            }
            int n = puntos.size();
            double[][] segundos = new double[n][n];
            for (int i = 0; i < n; i++) {
                List<Double> fila = respuesta.durations().get(i);
                if (fila == null || fila.size() != n) {
                    throw new IllegalStateException("Matriz incompleta");
                }
                for (int j = 0; j < n; j++) {
                    if (fila.get(j) == null) {
                        throw new IllegalStateException("Hay puntos sin recorrido por calles entre sí");
                    }
                    segundos[i][j] = fila.get(j);
                }
            }
            return new MatrizRecorrido(segundos, true, "tiempo de manejo estimado por calles (OSRM)");
        } catch (RestClientException | IllegalStateException | NullPointerException e) {
            LOG.warn("OSRM /table no disponible, se ordena en línea recta: {}", e.getMessage());
            return MatrizRecorrido.lineaRecta(puntos);
        }
    }

    /**
     * Recorrido por calles que pasa por los puntos EN EL ORDEN DADO (el orden lo decide el servicio de rutas).
     * Cada punto es {latitud, longitud}.
     */
    @Override
    public TrayectoDTO trazar(List<double[]> puntos) {
        if (puntos == null || puntos.size() < 2) {
            return TrayectoDTO.vacio();
        }
        String coordenadas = coordenadasOsrm(puntos);
        try {
            RespuestaOsrm respuesta = osrm.get()
                    .uri(URI.create(urlOsrm + "/route/v1/driving/" + coordenadas
                            + "?overview=full&geometries=geojson"))
                    .retrieve().body(RespuestaOsrm.class);
            if (respuesta == null || !"Ok".equals(respuesta.code()) || respuesta.routes() == null
                    || respuesta.routes().isEmpty()) {
                throw new IllegalStateException("OSRM no devolvió recorridos");
            }
            RutaOsrm ruta = respuesta.routes().get(0);
            List<List<Double>> geometria = ruta.geometry().coordinates().stream()
                    .map(c -> List.of(c.get(1), c.get(0)))
                    .toList();
            return new TrayectoDTO(geometria, ruta.distance() / 1000.0, ruta.duration() / 60.0,
                    "Recorrido por calles (OSRM, datos de OpenStreetMap)");
        } catch (RestClientException | IllegalStateException | NullPointerException e) {
            LOG.warn("OSRM no disponible, se informa línea recta: {}", e.getMessage());
            return lineaRecta(puntos);
        }
    }

    /** Línea recta entre puntos consecutivos. Se informa como tal: no es un recorrido por calles. */
    public static TrayectoDTO lineaRecta(List<double[]> puntos) {
        List<List<Double>> geometria = new ArrayList<>();
        double kilometros = 0;
        for (int i = 0; i < puntos.size(); i++) {
            geometria.add(List.of(puntos.get(i)[0], puntos.get(i)[1]));
            if (i > 0) {
                kilometros += CalculoDistancia.kilometros(puntos.get(i - 1)[0], puntos.get(i - 1)[1],
                        puntos.get(i)[0], puntos.get(i)[1]);
            }
        }
        return new TrayectoDTO(geometria, kilometros, 0,
                "Línea recta entre puntos (el servicio de recorridos por calles no respondió)");
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** OSRM recibe "longitud,latitud;longitud,latitud;…". */
    private static String coordenadasOsrm(List<double[]> puntos) {
        return puntos.stream()
                .map(p -> String.format(Locale.ROOT, "%.6f,%.6f", p[1], p[0]))
                .collect(Collectors.joining(";"));
    }

    private interface Consulta<T> {
        T ejecutar();
    }

    /** Respeta el límite de Nominatim (1 consulta por segundo) y traduce la falta de respuesta a 503. */
    private synchronized <T> T consultarNominatim(Consulta<T> consulta) throws ServicioExternoNoDisponibleException {
        long espera = ultimaConsultaNominatim + INTERVALO_MINIMO_MS - System.currentTimeMillis();
        if (espera > 0) {
            try {
                Thread.sleep(espera);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            return consulta.ejecutar();
        } catch (RestClientException e) {
            LOG.warn("Nominatim no disponible: {}", e.getMessage());
            throw new ServicioExternoNoDisponibleException(
                    "El servicio de búsqueda de direcciones no respondió. Marcá la ubicación tocando el mapa.");
        } finally {
            ultimaConsultaNominatim = System.currentTimeMillis();
        }
    }

    private Optional<GeocodificacionDTO> convertir(ResultadoNominatim r) {
        if (r == null || r.lat() == null || r.lon() == null) {
            return Optional.empty();
        }
        try {
            double latitud = Double.parseDouble(r.lat());
            double longitud = Double.parseDouble(r.lon());
            DireccionNominatim d = r.address() == null ? new DireccionNominatim(null, null, null, null, null,
                    null, null, null, null, null) : r.address();
            String localidad = primeroNoVacio(d.city(), d.town(), d.village(), d.suburb());
            return Optional.of(new GeocodificacionDTO(latitud, longitud, r.display_name(), d.road(),
                    d.house_number(), primeroNoVacio(d.neighbourhood(), d.suburb()), localidad, d.county(),
                    d.state(), d.postcode()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static String primeroNoVacio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }

    private static void validarCoordenadas(Double latitud, Double longitud) throws MiException {
        if (latitud == null || longitud == null || latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new MiException("Las coordenadas indicadas no son válidas");
        }
    }

    private synchronized Object leerCache(String clave) {
        return cache.get(clave);
    }

    private synchronized void guardarCache(String clave, Object valor) {
        cache.put(clave, valor);
    }

    // Formato de las respuestas externas (solo los campos que se usan)

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ResultadoNominatim(String lat, String lon, String display_name, DireccionNominatim address, String error) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DireccionNominatim(String road, String house_number, String neighbourhood, String suburb, String city,
                              String town, String village, String county, String state, String postcode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaOsrm(String code, List<RutaOsrm> routes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaTablaOsrm(String code, List<List<Double>> durations) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RutaOsrm(double distance, double duration, GeometriaOsrm geometry) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeometriaOsrm(List<List<Double>> coordinates) {
    }
}
