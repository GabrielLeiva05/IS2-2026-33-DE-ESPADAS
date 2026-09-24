package ar.com.biblioteca.client.api;

import ar.com.biblioteca.client.dto.ApiErrorDTO;
import ar.com.biblioteca.client.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Único punto de la aplicación que usa RestTemplate. Encapsula las operaciones HTTP
 * (GET, POST, PUT, DELETE) contra el servidor y traduce los errores a {@link ApiException}.
 * <p>
 * Las rutas reciben variables con la sintaxis {var}: p. ej. get("/personas/{id}", PersonaDTO.class, 5L)
 */
@Component
public class ApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public ApiClient(RestTemplate restTemplate,
                     ObjectMapper objectMapper,
                     @Value("${biblioteca.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    /** GET de un recurso. */
    public <T> T get(String path, Class<T> tipo, Object... variables) {
        return ejecutar(() -> restTemplate.getForObject(baseUrl + path, tipo, variables));
    }

    /** GET de una colección (se deserializa como arreglo y se devuelve como lista). */
    public <T> List<T> getList(String path, Class<T[]> tipoArreglo, Object... variables) {
        T[] arreglo = ejecutar(() -> restTemplate.getForObject(baseUrl + path, tipoArreglo, variables));
        return arreglo == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(arreglo));
    }

    /** POST: crea un recurso y devuelve el creado. */
    public <T> T post(String path, Object cuerpo, Class<T> tipo, Object... variables) {
        return ejecutar(() -> restTemplate.postForObject(baseUrl + path, cuerpo, tipo, variables));
    }

    /** PUT: actualiza un recurso. */
    public void put(String path, Object cuerpo, Object... variables) {
        ejecutar(() -> {
            restTemplate.put(baseUrl + path, cuerpo, variables);
            return null;
        });
    }

    /** DELETE: elimina un recurso. */
    public void delete(String path, Object... variables) {
        ejecutar(() -> {
            restTemplate.delete(baseUrl + path, variables);
            return null;
        });
    }

    private <T> T ejecutar(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (HttpStatusCodeException e) {
            throw traducir(e);
        } catch (ResourceAccessException e) {
            throw new ApiException(503, "No se pudo conectar con el servidor (" + baseUrl
                    + "). Verifique que la aplicación servidor esté en ejecución.");
        }
    }

    /** Convierte la respuesta de error del servidor (JSON) en una ApiException con un mensaje legible. */
    private ApiException traducir(HttpStatusCodeException e) {
        int status = e.getStatusCode().value();
        String mensaje = null;
        try {
            ApiErrorDTO error = objectMapper.readValue(e.getResponseBodyAsString(), ApiErrorDTO.class);
            mensaje = error.getMessage();
            if (mensaje != null && error.getFieldErrors() != null && !error.getFieldErrors().isEmpty()) {
                mensaje = mensaje + ": " + String.join("; ", error.getFieldErrors().values());
            }
        } catch (Exception ignorada) {
            // el cuerpo no era JSON: se usa el mensaje genérico
        }
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "El servidor respondió con el estado HTTP " + status;
        }
        return new ApiException(status, mensaje);
    }
}
