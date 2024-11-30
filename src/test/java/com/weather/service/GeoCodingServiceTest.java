package com.weather.service;

import com.weather.config.OpenWeatherConfig;
import com.weather.dao.PincodeLocationRepository;
import com.weather.dto.GeoCodingResponse;
import com.weather.model.PincodeLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GeoCodingServiceTest {

    @Mock
    private PincodeLocationRepository pincodeLocationRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OpenWeatherConfig openWeatherConfig;

    @InjectMocks
    private GeoCodingServiceImpl geoCodingService;

    private PincodeLocation existingPincodeLocation;
    private GeoCodingResponse mockGeoCodingResponse;

    @BeforeEach
    void setUp() {
        // Setup existing PincodeLocation
        existingPincodeLocation = new PincodeLocation();
        existingPincodeLocation.setPincode("580004");
        existingPincodeLocation.setLatitude(15.4287);
        existingPincodeLocation.setLongitude(75.0705);

        // Setup mock GeoCodingResponse
        mockGeoCodingResponse = new GeoCodingResponse();
        mockGeoCodingResponse.setZip("580004");
        mockGeoCodingResponse.setLat(15.4287);
        mockGeoCodingResponse.setLon(75.0705);
    }

    @Test
    void testGetLocationByPincode_WhenCachedInDatabase() {
        // Arrange
        when(pincodeLocationRepository.findByPincode("580004"))
            .thenReturn(Optional.of(existingPincodeLocation));

        // Act
        PincodeLocation result = geoCodingService.getLocationByPincode("580004");

        // Assert
        assertNotNull(result);
        assertEquals("580004", result.getPincode());
        assertEquals(15.4287, result.getLatitude());
        assertEquals(75.0705, result.getLongitude());
        
        // Verify that repository findByPincode was called
        verify(pincodeLocationRepository, times(1)).findByPincode("580004");
        
        // Verify that external API is not called when data is in cache
        verify(restTemplate, never()).getForObject(anyString(), eq(GeoCodingResponse.class));
    }

    @Test
    void testGetLocationByPincode_WhenNotCachedInDatabase() {
        // Arrange
        when(pincodeLocationRepository.findByPincode("580004"))
            .thenReturn(Optional.empty());
        
        when(openWeatherConfig.getApiKey())
            .thenReturn("test-api-key");
        
        when(restTemplate.getForObject(
            eq("http://api.openweathermap.org/geo/1.0/zip?zip=580004,IN&appid=test-api-key"), 
            eq(GeoCodingResponse.class)
        )).thenReturn(mockGeoCodingResponse);
        
        when(pincodeLocationRepository.save(any(PincodeLocation.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PincodeLocation result = geoCodingService.getLocationByPincode("580004");

        // Assert
        assertNotNull(result);
        assertEquals("580004", result.getPincode());
        assertEquals(15.4287, result.getLatitude());
        assertEquals(75.0705, result.getLongitude());
        
        // Verify method calls
        verify(pincodeLocationRepository, times(1)).findByPincode("580004");
        verify(restTemplate, times(1)).getForObject(anyString(), eq(GeoCodingResponse.class));
        verify(pincodeLocationRepository, times(1)).save(any(PincodeLocation.class));
    }

    @Test
    void testGetLocationByPincode_WhenApiReturnsNull() {
        // Arrange
        when(pincodeLocationRepository.findByPincode("580004"))
            .thenReturn(Optional.empty());
        
        when(openWeatherConfig.getApiKey())
            .thenReturn("test-api-key");
        
        when(restTemplate.getForObject(
            eq("http://api.openweathermap.org/geo/1.0/zip?zip=580004,IN&appid=test-api-key"), 
            eq(GeoCodingResponse.class)
        )).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            geoCodingService.getLocationByPincode("580004");
        });
        
        // Verify method calls
        verify(pincodeLocationRepository, times(1)).findByPincode("580004");
        verify(restTemplate, times(1)).getForObject(anyString(), eq(GeoCodingResponse.class));
        verify(pincodeLocationRepository, never()).save(any(PincodeLocation.class));
    }

    @Test
    void testGetLocationByPincode_UrlFormatVerification() {
        // Arrange
        when(pincodeLocationRepository.findByPincode("580004"))
            .thenReturn(Optional.empty());
        
        when(openWeatherConfig.getApiKey())
            .thenReturn("test-api-key");
        
        when(restTemplate.getForObject(
            eq("http://api.openweathermap.org/geo/1.0/zip?zip=580004,IN&appid=test-api-key"), 
            eq(GeoCodingResponse.class)
        )).thenReturn(mockGeoCodingResponse);
        
        when(pincodeLocationRepository.save(any(PincodeLocation.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Assert
        // Verify that the correct URL is constructed
        verify(restTemplate).getForObject(
            eq("http://api.openweathermap.org/geo/1.0/zip?zip=580004,IN&appid=test-api-key"), 
            eq(GeoCodingResponse.class)
        );
    }
}