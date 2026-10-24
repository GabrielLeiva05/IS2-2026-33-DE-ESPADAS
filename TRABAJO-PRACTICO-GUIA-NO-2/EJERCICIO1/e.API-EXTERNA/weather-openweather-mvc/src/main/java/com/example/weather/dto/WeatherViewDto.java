package com.example.weather.dto;

public class WeatherViewDto {

    private final String city;
    private final String country;
    private final double temperature;
    private final double feelsLike;
    private final int humidity;
    private final String description;
    private final String iconUrl;
    private final double windSpeed;

    public WeatherViewDto(String city, String country, double temperature,
                          double feelsLike, int humidity, String description,
                          String iconUrl, double windSpeed) {
        this.city = city;
        this.country = country;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
        this.iconUrl = iconUrl;
        this.windSpeed = windSpeed;
    }

    public String getCity() { return city; }
    public String getCountry() { return country; }
    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public int getHumidity() { return humidity; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public double getWindSpeed() { return windSpeed; }
}
