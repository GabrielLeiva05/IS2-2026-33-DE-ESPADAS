package com.dulcemayor.admin.cliente;

import com.dulcemayor.admin.dto.ErrorApiDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Capa de acceso a datos de admin-web: en lugar de un repositorio JPA, los datos viven en la API REST
 * del backend. Solo los servicios usan esta clase; los controladores nunca la llaman directamente.
 * El token JWT viaja en el encabezado Authorization y nunca se expone al navegador.
 */
@Component
public class ClienteApi {

    private static final String SIN_CONEXION =
            "No se pudo conectar con el servidor de Dulce Mayor. Verificá que la API esté en ejecución.";

    private final RestClient restClient;

    public ClienteApi(@Value("${api.base-url}") String urlBase) {
        this.restClient = RestClient.builder()
                .baseUrl(urlBase)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public <T> T obtener(String token, Class<T> tipo, String uri, Object... variables) {
        return ejecutar(() -> restClient.get().uri(uri, variables)
                .headers(h -> autorizar(h, token))
                .retrieve().body(tipo));
    }

    /** GET de una colección: la API devuelve un arreglo JSON. */
    public <T> List<T> listar(String token, Class<T[]> tipoArreglo, String uri, Object... variables) {
        T[] arreglo = obtener(token, tipoArreglo, uri, variables);
        return arreglo == null ? List.of() : Arrays.asList(arreglo);
    }

    /** GET de una colección con parámetros de consulta opcionales (los vacíos no se envían). */
    public <T> List<T> listarConFiltros(String token, Class<T[]> tipoArreglo, String ruta, Map<String, String> filtros) {
        Map<String, String> presentes = new LinkedHashMap<>();
        filtros.forEach((clave, valor) -> {
            if (valor != null && !valor.isBlank()) {
                presentes.put(clave, valor.trim());
            }
        });
        T[] arreglo = ejecutar(() -> restClient.get()
                .uri(constructor -> {
                    constructor.path(ruta);
                    presentes.keySet().forEach(clave -> constructor.queryParam(clave, "{" + clave + "}"));
                    return constructor.build(presentes);
                })
                .headers(h -> autorizar(h, token))
                .retrieve().body(tipoArreglo));
        return arreglo == null ? List.of() : Arrays.asList(arreglo);
    }

    public <T> T enviar(String token, Object cuerpo, Class<T> tipo, String uri, Object... variables) {
        return ejecutar(() -> restClient.post().uri(uri, variables)
                .headers(h -> autorizar(h, token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve().body(tipo));
    }

    public <T> T reemplazar(String token, Object cuerpo, Class<T> tipo, String uri, Object... variables) {
        return ejecutar(() -> restClient.put().uri(uri, variables)
                .headers(h -> autorizar(h, token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve().body(tipo));
    }

    /** PATCH con cuerpo JSON (por ejemplo, reponer stock). */
    public <T> T parchear(String token, Object cuerpo, Class<T> tipo, String uri, Object... variables) {
        return ejecutar(() -> restClient.patch().uri(uri, variables)
                .headers(h -> autorizar(h, token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve().body(tipo));
    }

    /** POST multipart (subida de una imagen). El archivo viaja tal cual a la API, que valida tipo y tamaño. */
    public <T> T enviarArchivo(String token, org.springframework.util.MultiValueMap<String, Object> partes, Class<T> tipo,
                               String uri, Object... variables) {
        return ejecutar(() -> restClient.post().uri(uri, variables)
                .headers(h -> autorizar(h, token))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(partes)
                .retrieve().body(tipo));
    }

    /** GET de un archivo binario (por ejemplo, el PDF de una factura). */
    public byte[] obtenerArchivo(String token, String uri, Object... variables) {
        return ejecutar(() -> restClient.get().uri(uri, variables)
                .headers(h -> autorizar(h, token))
                .accept(MediaType.APPLICATION_PDF, MediaType.APPLICATION_OCTET_STREAM)
                .retrieve().body(byte[].class));
    }

    public void eliminar(String token, String uri, Object... variables) {
        ejecutar(() -> restClient.delete().uri(uri, variables)
                .headers(h -> autorizar(h, token))
                .retrieve().toBodilessEntity());
    }

    private void autorizar(HttpHeaders encabezados, String token) {
        if (token != null && !token.isBlank()) {
            encabezados.setBearerAuth(token);
        }
    }

    /** Traduce los errores HTTP de la API (formato ErrorDTO) a ApiException con un mensaje legible. */
    private <T> T ejecutar(Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (RestClientResponseException e) {
            String mensaje = null;
            try {
                ErrorApiDTO error = e.getResponseBodyAs(ErrorApiDTO.class);
                mensaje = error == null ? null : error.getMensaje();
            } catch (RuntimeException cuerpoIlegible) {
                // La respuesta no tenía el formato ErrorDTO: se usa un mensaje genérico.
            }
            if (mensaje == null || mensaje.isBlank()) {
                mensaje = "La operación no se pudo completar (código " + e.getStatusCode().value() + ")";
            }
            String accion = e.getResponseHeaders() == null ? null
                    : e.getResponseHeaders().getFirst("X-Accion-Requerida");
            throw new ApiException(e.getStatusCode().value(), mensaje, accion);
        } catch (ResourceAccessException e) {
            throw new ApiException(503, SIN_CONEXION, null);
        }
    }
}
