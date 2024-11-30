package com.weather.controller;

import com.weather.dto.WeatherResponse;
import com.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;
    private WeatherResponse mockWeatherResponse;

    @BeforeEach
    void setUp() {
        mockWeatherResponse = createMockWeatherResponse();
    }

    private WeatherResponse createMockWeatherResponse() {
        WeatherResponse response = new WeatherResponse();
        response.setPincode("580004");
        response.setDate(LocalDate.of(2024, 10, 10));
        response.setDescription("The weather in Dharwad, India, is currently scattered clouds with a temperature of 25.85°C, feeling like 26.08°C. The humidity is at 61%, and the wind is blowing at 5.14 m/s. Visiblity is 6.0 km, and atmospheric pressure is 1011 hPa at sea level.");
        return response;
    }

    @SuppressWarnings("deprecation")
	@Test
    void testGetWeatherInfo_Success() {
        // Arrange
        when(weatherService.getWeatherForPincodeAndDate(anyString(), any(LocalDate.class)))
            .thenReturn(mockWeatherResponse);

        // Act
        ResponseEntity<WeatherResponse> response = weatherController.getWeatherInfo("580004", LocalDate.of(2024, 10, 10));

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockWeatherResponse, response.getBody());
        verify(weatherService, times(1)).getWeatherForPincodeAndDate(anyString(), any(LocalDate.class));
    }
}