package com.example.weather.service;

import com.example.weather.dto.OpenWeatherResponse;
import com.example.weather.dto.WeatherViewDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public WeatherService(
            RestTemplate restTemplate,
            @Value("${openweather.api-key}") String apiKey,
            @Value("${openweather.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public WeatherViewDto getCurrentWeather(String city) {

        String normalizedCity = normalizeCity(city);

        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .queryParam("q", normalizedCity)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "es")
                .toUriString();

        System.out.println("CIUDAD: [" + normalizedCity + "]");
        System.out.println("URL: " + url);

        try {
            System.out.println("ANTES DE RESTTEMPLATE");

            URI uri = URI.create(url);

            System.out.println("URI: " + uri);

            OpenWeatherResponse response =
                    restTemplate.getForObject(uri, OpenWeatherResponse.class);

            System.out.println("DESPUÉS DE RESTTEMPLATE");
            System.out.println("RESPONSE: " + response);

            if (response == null || response.getMain() == null
                    || response.getWeather() == null
                    || response.getWeather().isEmpty()) {

                throw new IllegalStateException(
                        "OpenWeather no devolvió información meteorológica válida."
                );
            }

            OpenWeatherResponse.WeatherData condition =
                    response.getWeather().get(0);

            System.out.println(
                    "CONDICIÓN: " + condition.getDescription()
            );

            return new WeatherViewDto(
                    response.getName(),
                    response.getSys() != null
                            ? response.getSys().getCountry()
                            : "AR",
                    response.getMain().getTemp(),
                    response.getMain().getFeels_like(),
                    response.getMain().getHumidity(),
                    condition.getDescription(),
                    "https://openweathermap.org/img/wn/"
                            + condition.getIcon() + "@2x.png",
                    response.getWind() != null
                            ? response.getWind().getSpeed()
                            : 0
            );

        } catch (HttpClientErrorException e) {

            System.out.println("ENTRÓ AL CATCH HTTP");
            System.out.println("STATUS: " + e.getStatusCode());
            System.out.println(
                    "BODY: " + e.getResponseBodyAsString()
            );

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalArgumentException(
                        "No se encontró la ciudad indicada."
                );
            }

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new IllegalStateException(
                        "La API key de OpenWeather no es válida "
                                + "o todavía no está activa."
                );
            }

            throw new IllegalStateException(
                    "OpenWeather rechazó la solicitud: "
                            + e.getStatusCode()
            );

        } catch (Exception e) {

            System.out.println("ENTRÓ AL CATCH GENERAL");
            System.out.println(
                    "TIPO: " + e.getClass().getName()
            );
            System.out.println(
                    "MENSAJE: " + e.getMessage()
            );

            if (e instanceof IllegalArgumentException
                    || e instanceof IllegalStateException) {
                throw e;
            }

            throw new IllegalStateException(
                    "No fue posible consultar "
                            + "el servicio meteorológico.",
                    e
            );
        }
    }

    private String normalizeCity(String city) {

        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar una ciudad."
            );
        }

        String normalized = city.trim();

        if (normalized.length() > 80) {
            throw new IllegalArgumentException(
                    "El nombre de la ciudad es demasiado largo."
            );
        }

        return normalized;
    }
}