package com.example.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
        String name,
        Main main,
        List<Condition> weather,
        Wind wind,
        Sys sys
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(double temp, double feels_like, int humidity, int pressure) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Condition(String description, String icon) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(double speed) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sys(String country) {
    }
}
