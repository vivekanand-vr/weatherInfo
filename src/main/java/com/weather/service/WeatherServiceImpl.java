package com.weather.service;

import com.weather.config.OpenWeatherConfig;
import com.weather.dto.WeatherApiResponse;
import com.weather.dto.WeatherResponse;
import com.weather.model.PincodeLocation;
import com.weather.model.WeatherInfo;
import com.weather.dao.WeatherInfoRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {
    private final GeoCodingService geoCodingService;
    private final WeatherInfoRepository weatherInfoRepository;
    private final RestTemplate restTemplate;
    private final OpenWeatherConfig openWeatherConfig;

    @Override
    public WeatherResponse getWeatherForPincodeAndDate(String pincode, LocalDate date) {
        
    	// Check existing weather info
        Optional<WeatherInfo> existingWeatherInfo = 
            weatherInfoRepository.findByPincodeAndWeatherDate(pincode, date);
        
        if (existingWeatherInfo.isPresent()) {
            return convertToResponse(existingWeatherInfo.get());
        }

        // Fetch location and weather
        PincodeLocation location =  geoCodingService.getLocationByPincode(pincode);
        return fetchWeatherFromApi(location, date);    
    }

    private WeatherResponse fetchWeatherFromApi(PincodeLocation location, LocalDate date) {
        
        String weatherUrl = String.format(
            "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
            location.getLatitude(), location.getLongitude(), openWeatherConfig.getApiKey()
        );
        
        // Making an API call to OpenWeather to get the weather info
        WeatherApiResponse weatherResponse = restTemplate.getForObject(weatherUrl, WeatherApiResponse.class);

        if (weatherResponse == null) {
            throw new IllegalStateException("Unable to fetch weather information.");
        }
        
        // Convert API response to response DTO
        WeatherResponse responseDto = convertApiToResponse(location, weatherResponse, date);
        
        // Just save to database (after converting to WeatherInfo object)
        weatherInfoRepository.save(convertToInfo(responseDto));
        
        return responseDto;
    }
    
    @Override
	public WeatherResponse convertApiToResponse(PincodeLocation location, WeatherApiResponse weatherResponse, LocalDate date) {
    	WeatherResponse dto = new WeatherResponse();
    	dto.setPincode(location.getPincode());
    	dto.setDate(date);
    	dto.setDescription("The weather in " + 
    			  weatherResponse.getName() + ", India, is currently " +
    			  weatherResponse.getWeather().get(0).getDescription() + " with a temperature of " +
    			  weatherResponse.getMain().getTemp() + "°C, feeling like " +
    			  weatherResponse.getMain().getFeelsLike() +  "°C. The humidity is at " +
    			  weatherResponse.getMain().getHumidity() + "%, and the wind is blowing at " + 
    			  weatherResponse.getWind().getSpeed() + " m/s. Visiblity is " + 
    			  (double)weatherResponse.getVisibility()/1000 + " km, and atmospheric pressure is " +
    			  weatherResponse.getMain().getPressure() + " hPa at sea level."			  
    	);
    	return dto;
    }
    
    @Override
    public WeatherInfo convertToInfo(WeatherResponse responseDto) {
        WeatherInfo dto = new WeatherInfo();
        dto.setPincode(responseDto.getPincode());
        dto.setWeatherDate(responseDto.getDate());
        dto.setDescription(responseDto.getDescription());
        return dto;
    }
    
    @Override
    public WeatherResponse convertToResponse(WeatherInfo weatherInfo) {
        WeatherResponse resp = new WeatherResponse();
        resp.setPincode(weatherInfo.getPincode());
        resp.setDate(weatherInfo.getWeatherDate());
        resp.setDescription(weatherInfo.getDescription());
        return resp;
    }
}