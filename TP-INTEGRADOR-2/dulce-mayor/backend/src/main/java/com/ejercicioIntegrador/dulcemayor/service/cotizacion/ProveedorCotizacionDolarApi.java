package com.ejercicioIntegrador.dulcemayor.service.cotizacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * Cotización desde DolarAPI (https://dolarapi.com), gratuita y sin clave.
 * GET {url}/v1/dolares/{tipo} → {"moneda","casa","nombre","compra","venta","fechaActualizacion"}.
 */
@Component
public class ProveedorCotizacionDolarApi implements ProveedorCotizacion {

    private final RestClient cliente;
    private final String url;

    public ProveedorCotizacionDolarApi(@Value("${app.cotizacion.url:https://dolarapi.com}") String url) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(3000);
        tiempos.setReadTimeout(5000);
        this.cliente = RestClient.builder().requestFactory(tiempos).build();
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Override
    public Cotizacion consultar(String tipo) {
        RespuestaDolarApi respuesta;
        try {
            respuesta = cliente.get().uri(URI.create(url + "/v1/dolares/" + tipo)).retrieve().body(RespuestaDolarApi.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("DolarAPI no respondió: " + e.getMessage(), e);
        }
        if (respuesta == null || respuesta.venta() == null || respuesta.venta().signum() <= 0) {
            throw new IllegalStateException("DolarAPI devolvió una cotización inválida");
        }
        return new Cotizacion(tipo, respuesta.nombre() == null ? tipo : respuesta.nombre(), respuesta.compra(),
                respuesta.venta(), fecha(respuesta.fechaActualizacion()));
    }

    static OffsetDateTime fecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaDolarApi(String moneda, String casa, String nombre, BigDecimal compra, BigDecimal venta,
                             String fechaActualizacion) {
    }
}
