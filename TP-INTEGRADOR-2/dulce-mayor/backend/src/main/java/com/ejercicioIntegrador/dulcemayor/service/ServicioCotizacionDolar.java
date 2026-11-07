package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.CotizacionDolarDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Cotización del dólar del BCRA (variable 5, tipo de cambio minorista). Consulta con timeout y
 * caché en memoria; si el proveedor no responde se conserva el último valor y se marca como no
 * actualizado.
 */
@Service
public class ServicioCotizacionDolar {

    private static final Logger LOG = LoggerFactory.getLogger(ServicioCotizacionDolar.class);

    private final JsonMapper json = JsonMapper.builder().build();
    private final HttpClient http;
    private final String url;
    private final Duration timeout;
    private final Duration vigenciaCache;

    private CotizacionDolarDTO ultima;
    private LocalDateTime consultadoEn;

    public ServicioCotizacionDolar(
            @Value("${app.cotizacion.url:https://api.bcra.gob.ar/estadisticas/v2.0/datosvariable/5}") String url,
            @Value("${app.cotizacion.timeout-ms:4000}") long timeoutMs,
            @Value("${app.cotizacion.cache-minutos:30}") long cacheMinutos) {
        this.url = url;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.vigenciaCache = Duration.ofMinutes(cacheMinutos);
        this.http = HttpClient.newBuilder().connectTimeout(this.timeout).build();
    }

    public synchronized CotizacionDolarDTO obtener() {
        if (ultima != null && consultadoEn != null
                && consultadoEn.plus(vigenciaCache).isAfter(LocalDateTime.now())) {
            return ultima;
        }
        try {
            HttpRequest pedido = HttpRequest.newBuilder(URI.create(url)).timeout(timeout)
                    .header("Accept", "application/json").GET().build();
            HttpResponse<String> respuesta = http.send(pedido, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() >= 300) {
                return sinActualizar("El BCRA respondió " + respuesta.statusCode());
            }
            CotizacionDolarDTO leida = interpretar(respuesta.body());
            if (leida == null) {
                return sinActualizar("El BCRA devolvió una respuesta inesperada");
            }
            ultima = leida;
            consultadoEn = LocalDateTime.now();
            return leida;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return sinActualizar("La consulta al BCRA fue interrumpida");
        } catch (Exception e) {
            LOG.warn("No se pudo obtener la cotización del BCRA: {}", e.getMessage());
            return sinActualizar("No se pudo consultar el BCRA");
        }
    }

    private CotizacionDolarDTO interpretar(String cuerpo) {
        try {
            JsonNode raiz = json.readTree(cuerpo);
            JsonNode resultados = raiz.path("results");
            if (!resultados.isArray() || resultados.isEmpty()) {
                return null;
            }
            JsonNode ultimo = resultados.get(resultados.size() - 1);
            JsonNode valor = ultimo.get("valor");
            if (valor == null || valor.isNull()) {
                return null;
            }
            return new CotizacionDolarDTO(valor.decimalValue(), LocalDateTime.now(), true,
                    "Cotización informativa del BCRA");
        } catch (RuntimeException e) {
            return null;
        }
    }

    private CotizacionDolarDTO sinActualizar(String motivo) {
        if (ultima != null) {
            return new CotizacionDolarDTO(ultima.valor(), ultima.fechaActualizacion(), false,
                    motivo + ": se muestra el último valor conocido");
        }
        return new CotizacionDolarDTO(null, null, false, motivo);
    }
}
