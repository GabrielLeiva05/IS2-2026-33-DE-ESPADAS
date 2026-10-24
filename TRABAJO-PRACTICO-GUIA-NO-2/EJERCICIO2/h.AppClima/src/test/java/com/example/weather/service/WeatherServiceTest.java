package com.example.weather.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherServiceTest {

    private final WeatherService service = new WeatherService(
            RestClient.builder().build(),
            "test-key",
            "https://example.com/weather"
    );

    @Test
    void rejectsBlankCityBeforeCallingExternalService() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getCurrentWeather("   ")
        );

        assertEquals("Ingresa el nombre de una ciudad.", exception.getMessage());
    }

    @Test
    void refusesToCallExternalServiceWithoutApiKey() {
        WeatherService serviceWithoutKey = new WeatherService(
                RestClient.builder().build(),
                "",
                "https://example.com/weather"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> serviceWithoutKey.getCurrentWeather("Mendoza")
        );

        assertEquals(
                "Falta configurar OPENWEATHER_API_KEY en el entorno del servidor.",
                exception.getMessage()
        );
    }
}
