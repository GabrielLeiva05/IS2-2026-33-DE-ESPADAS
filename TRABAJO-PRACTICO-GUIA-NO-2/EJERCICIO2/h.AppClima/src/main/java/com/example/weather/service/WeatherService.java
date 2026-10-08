package com.example.weather.service;

import com.example.weather.dto.OpenWeatherResponse;
import com.example.weather.dto.WeatherDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class WeatherService {

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;

    public WeatherService(
            RestClient weatherRestClient,
            @Value("${openweather.api-key}") String apiKey,
            @Value("${openweather.base-url}") String baseUrl) {
        this.restClient = weatherRestClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public WeatherDto getCurrentWeather(String city) {
        String normalizedCity = normalizeCity(city);
        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar OPENWEATHER_API_KEY en el entorno del servidor."
            );
        }

        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("q", normalizedCity)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "es")
                .encode()
                .build()
                .toUri();

        try {
            OpenWeatherResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OpenWeatherResponse.class);

            if (response == null || response.main() == null
                    || response.weather() == null || response.weather().isEmpty()) {
                throw new IllegalStateException(
                        "El servicio meteorológico devolvió una respuesta incompleta."
                );
            }

            OpenWeatherResponse.Condition condition = response.weather().get(0);
            return new WeatherDto(
                    response.name(),
                    response.sys() == null ? "" : response.sys().country(),
                    response.main().temp(),
                    response.main().feels_like(),
                    response.main().humidity(),
                    response.main().pressure(),
                    condition.description(),
                    condition.icon(),
                    response.wind() == null ? 0 : response.wind().speed()
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new IllegalArgumentException("No encontramos esa ciudad.");
            }
            if (exception.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                throw new IllegalStateException(
                        "La clave de OpenWeather no es válida o aún no está activa."
                );
            }
            if (exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new IllegalStateException(
                        "Se alcanzó el límite de consultas. Inténtalo de nuevo más tarde."
                );
            }
            throw new IllegalStateException(
                    "OpenWeather rechazó la consulta (HTTP "
                            + exception.getStatusCode().value() + ").",
                    exception
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "No fue posible conectar con el servicio meteorológico.",
                    exception
            );
        }
    }

    private String normalizeCity(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Ingresa el nombre de una ciudad.");
        }

        String normalizedCity = city.trim();
        if (normalizedCity.length() > 80) {
            throw new IllegalArgumentException("El nombre de la ciudad es demasiado largo.");
        }
        return normalizedCity;
    }
}
