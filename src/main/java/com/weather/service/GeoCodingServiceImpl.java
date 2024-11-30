package com.weather.service;

import com.weather.config.OpenWeatherConfig;
import com.weather.model.PincodeLocation;
import com.weather.dao.PincodeLocationRepository;
import com.weather.dto.GeoCodingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoCodingServiceImpl implements GeoCodingService {
    private final PincodeLocationRepository pincodeLocationRepository;
    private final RestTemplate restTemplate;
    private final OpenWeatherConfig openWeatherConfig;

    @Override
    public PincodeLocation getLocationByPincode(String pincode) {
        
    	// First, check if location exists in database
    	log.debug("Checking the Pincode exitance in the Cache");
        Optional<PincodeLocation> existingLocation = 
            pincodeLocationRepository.findByPincode(pincode);
        
        if (existingLocation.isPresent()) {
            return existingLocation.get();
        }
        
        // Fetch latitude/longitude from Geolocation API
        log.debug("Fetching Latitude/Longitude using pincode");
        String geocodeUrl = String.format(
            "http://api.openweathermap.org/geo/1.0/zip?zip=%s,IN&appid=%s",
            pincode, openWeatherConfig.getApiKey()
        );
        
        GeoCodingResponse geocode = restTemplate.getForObject(geocodeUrl, GeoCodingResponse.class);
        
        // Convert GeoCodingResponse to PincodeLocation
        PincodeLocation location = new PincodeLocation();
        location.setLatitude(geocode.getLat());
        location.setLongitude(geocode.getLon());
        location.setPincode(geocode.getZip());
        
        // Save the location details in DB and return
        return pincodeLocationRepository.save(location);
    }
}