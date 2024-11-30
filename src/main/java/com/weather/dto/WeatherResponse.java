package com.weather.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WeatherResponse {
    private String pincode;
    private LocalDate date;
    private String description;
}