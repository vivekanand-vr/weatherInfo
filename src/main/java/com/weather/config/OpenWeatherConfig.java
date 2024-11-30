package com.weather.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class OpenWeatherConfig {
    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.api.base-url:https://api.openweathermap.org/data/2.5}")
    private String baseUrl;

    @Value("${openweathermap.api.geocoding-url:http://api.openweathermap.org/geo/1.0}")
    private String geocodingUrl;
}