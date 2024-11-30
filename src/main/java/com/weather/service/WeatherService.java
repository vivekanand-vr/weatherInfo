package com.weather.service;

import com.weather.dto.WeatherApiResponse;
import com.weather.dto.WeatherResponse;
import com.weather.model.PincodeLocation;
import com.weather.model.WeatherInfo;

import java.time.LocalDate;

public interface WeatherService {
    WeatherResponse getWeatherForPincodeAndDate(
        String pincode, 
        LocalDate date);
    WeatherResponse convertApiToResponse(
    		PincodeLocation location, WeatherApiResponse weatherResponse, LocalDate date);
    WeatherInfo convertToInfo(WeatherResponse responseDto);
    WeatherResponse convertToResponse(WeatherInfo weatherInfo);
}