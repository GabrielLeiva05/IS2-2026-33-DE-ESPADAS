package com.example.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse {

    private String name;
    private MainData main;
    private List<WeatherData> weather;
    private WindData wind;
    private SysData sys;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public MainData getMain() { return main; }
    public void setMain(MainData main) { this.main = main; }

    public List<WeatherData> getWeather() { return weather; }
    public void setWeather(List<WeatherData> weather) { this.weather = weather; }

    public WindData getWind() { return wind; }
    public void setWind(WindData wind) { this.wind = wind; }

    public SysData getSys() { return sys; }
    public void setSys(SysData sys) { this.sys = sys; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MainData {
        private double temp;
        private double feels_like;
        private int humidity;

        public double getTemp() { return temp; }
        public void setTemp(double temp) { this.temp = temp; }

        public double getFeels_like() { return feels_like; }
        public void setFeels_like(double feels_like) { this.feels_like = feels_like; }

        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherData {
        private String description;
        private String icon;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WindData {
        private double speed;

        public double getSpeed() { return speed; }
        public void setSpeed(double speed) { this.speed = speed; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SysData {
        private String country;

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
}
