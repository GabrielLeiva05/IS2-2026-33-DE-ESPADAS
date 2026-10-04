package com.example.weather.controller;

import com.example.weather.dto.WeatherViewDto;
import com.example.weather.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public String home(
            @RequestParam(required = false, defaultValue = "Mendoza") String city,
            Model model) {

        model.addAttribute("searchedCity", city);

        try {
            WeatherViewDto weather = weatherService.getCurrentWeather(city);
            model.addAttribute("weather", weather);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "index";
    }
}
