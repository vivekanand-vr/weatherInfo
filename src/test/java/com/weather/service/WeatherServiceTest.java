package com.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.weather.config.OpenWeatherConfig;
import com.weather.dao.WeatherInfoRepository;
import com.weather.dto.WeatherApiResponse;
import com.weather.dto.WeatherResponse;
import com.weather.model.PincodeLocation;
import com.weather.model.WeatherInfo;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @Mock
    private GeoCodingService geoCodingService;

    @Mock
    private WeatherInfoRepository weatherInfoRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OpenWeatherConfig openWeatherConfig;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private PincodeLocation mockLocation;
    private WeatherApiResponse mockWeatherApiResponse;
    private WeatherInfo mockWeatherInfo;

    @BeforeEach
    void setUp() {
        // Setup mock location
        mockLocation = new PincodeLocation();
        mockLocation.setPincode("580004");
        mockLocation.setLatitude(15.4287);
        mockLocation.setLongitude(75.0705);

        // Setup mock WeatherApiResponse
        mockWeatherApiResponse = createMockWeatherApiResponse();

        // Setup mock WeatherInfo
        mockWeatherInfo = createMockWeatherInfo();
    }

    private WeatherApiResponse createMockWeatherApiResponse() {
        WeatherApiResponse response = new WeatherApiResponse();
        response.setName("Dharwad");
        
        WeatherApiResponse.Main main = new WeatherApiResponse.Main();
        main.setTemp(25.85);
        main.setFeelsLike(26.08);
        main.setHumidity(61);
        main.setPressure(1011);
        response.setMain(main);

        WeatherApiResponse.Weather weather = new WeatherApiResponse.Weather();
        weather.setDescription("scattered clouds");
        response.setWeather(Collections.singletonList(weather));

        WeatherApiResponse.Wind wind = new WeatherApiResponse.Wind();
        wind.setSpeed(5.14);
        response.setWind(wind);
        response.setVisibility(6000);

        return response;
    }

    private WeatherInfo createMockWeatherInfo() {
        WeatherInfo info = new WeatherInfo();
        info.setPincode("580004");
        info.setWeatherDate(LocalDate.of(2024, 10, 10));
        info.setDescription("The weather in Dharwad, India, is currently scattered clouds with a temperature of 25.85°C, feeling like 26.08°C. The humidity is at 61%, and the wind is blowing at 5.14 m/s. Visiblity is 6.0 km, and atmospheric pressure is 1011 hPa at sea level.");
        return info;
    }

    @Test
    void testGetWeatherForPincodeAndDate_WhenCached() {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 10, 10);
        when(weatherInfoRepository.findByPincodeAndWeatherDate("580004", testDate))
            .thenReturn(Optional.of(mockWeatherInfo));

        // Act
        WeatherResponse response = weatherService.getWeatherForPincodeAndDate("580004", testDate);

        // Assert
        assertNotNull(response);
        assertEquals("580004", response.getPincode());
        assertEquals(testDate, response.getDate());
        assertEquals(mockWeatherInfo.getDescription(), response.getDescription());
        verify(weatherInfoRepository, times(1)).findByPincodeAndWeatherDate(anyString(), any(LocalDate.class));
        verify(geoCodingService, never()).getLocationByPincode(anyString());
    }

    @Test
    void testGetWeatherForPincodeAndDate_WhenNotCached() {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 10, 10);
        when(weatherInfoRepository.findByPincodeAndWeatherDate("580004", testDate))
            .thenReturn(Optional.empty());
        when(geoCodingService.getLocationByPincode("580004")).thenReturn(mockLocation);
        when(openWeatherConfig.getApiKey()).thenReturn("test-api-key");
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
            .thenReturn(mockWeatherApiResponse);

        // Act
        WeatherResponse response = weatherService.getWeatherForPincodeAndDate("580004", testDate);

        // Assert
        assertNotNull(response);
        assertEquals("580004", response.getPincode());
        assertEquals(testDate, response.getDate());
        assertTrue(response.getDescription().contains("Dharwad"));
        assertTrue(response.getDescription().contains("scattered clouds"));
        assertTrue(response.getDescription().contains("25.85°C"));
        verify(weatherInfoRepository, times(1)).save(any(WeatherInfo.class));
    }

    @Test
    void testGetWeatherForPincodeAndDate_ApiFailure() {
        // Arrange
        LocalDate testDate = LocalDate.of(2024, 10, 10);
        when(weatherInfoRepository.findByPincodeAndWeatherDate("580004", testDate))
            .thenReturn(Optional.empty());
        when(geoCodingService.getLocationByPincode("580004")).thenReturn(mockLocation);
        when(openWeatherConfig.getApiKey()).thenReturn("test-api-key");
        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
            .thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            weatherService.getWeatherForPincodeAndDate("580004", testDate);
        });
    }

    @Test
    void testConvertApiToResponse_FullDetailVerification() {
        // Arrange
        PincodeLocation location = new PincodeLocation();
        location.setPincode("580004");
        LocalDate testDate = LocalDate.of(2024, 10, 10);

        // Act
        WeatherResponse response = weatherService.convertApiToResponse(location, mockWeatherApiResponse, testDate);

        // Assert
        assertEquals("580004", response.getPincode());
        assertEquals(testDate, response.getDate());
        
        // Detailed description verification
        String description = response.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("Dharwad, India"));
        assertTrue(description.contains("scattered clouds"));
        assertTrue(description.contains("25.85°C"));
        assertTrue(description.contains("26.08°C"));
        assertTrue(description.contains("61%"));
        assertTrue(description.contains("5.14 m/s"));
        assertTrue(description.contains("6.0 km"));
        assertTrue(description.contains("1011 hPa"));
    }
}