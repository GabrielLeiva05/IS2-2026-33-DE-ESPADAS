package com.example.weather.service;

import com.example.weather.dto.WeatherViewDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class WeatherServiceTest {

    @Test
    void debeRechazarCiudadVacia() {
        WeatherService service = new WeatherService(
                new RestTemplate(),
                "test-key",
                "https://example.com/weather"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getCurrentWeather("   ")
        );

        assertEquals("Debe ingresar una ciudad.", exception.getMessage());
    }
}
