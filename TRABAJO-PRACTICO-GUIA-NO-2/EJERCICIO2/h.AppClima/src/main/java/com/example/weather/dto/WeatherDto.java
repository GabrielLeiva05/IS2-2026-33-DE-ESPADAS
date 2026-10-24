package com.example.weather.dto;

public record WeatherDto(
        String city,
        String country,
        double temperature,
        double feelsLike,
        int humidity,
        int pressure,
        String description,
        String icon,
        double windSpeed
) {
}
