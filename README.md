# Weather Information Service

## Overview

This is a Spring Boot-based Weather Information Service that provides detailed weather information for specific Indian pincodes. The application fetches real-time weather data from OpenWeatherMap API and offers caching mechanisms to improve performance and reduce external API calls.

### Features

- Retrieve weather information by pincode
- Geocoding support for Indian pincodes
- Caching of location and weather data
- Detailed weather descriptions
- Support for multiple date-based queries

### Technology Stack

- Java 17+, Spring Boot, Spring Web, OpenWeatherMap API, JPA/Hibernate, Maven, JUnit 5, Mockito

### Prerequisites

- Java 17, Maven 3.6+, OpenWeatherMap API Key

## Low-Level Design (LLD)

![WeatherInfoStructure](https://github.com/user-attachments/assets/ffde272d-ef9f-4396-80cd-976728c63b4f)


## Configuration

### Application Properties

Create `application.properties` or `application.yml` with the following configurations:

```properties
# OpenWeather API Configuration
openweather.api.key=YOUR_API_KEY_HERE

# Database Configuration
spring.datasource.url=jdbc:h2:mem:weatherdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

## Installation

1. Clone the repository
```
git clone https://github.com/yourusername/weather-information-service.git
```

2. Navigate to project directory
```
cd weather-information-service
```

3. Configure OpenWeatherMap API Key
- Replace `YOUR_API_KEY_HERE` in `application.properties`
- You can get a free API key from [OpenWeatherMap](https://openweathermap.org/api)

4. Build the project
```
mvn clean install
```

## Running the Application

```
mvn spring-boot:run
```

## API Endpoints

### Get Weather Information

- **Endpoint:** `/weather`
- **Method:** GET
- **Parameters:**
  - `pincode`: Indian pincode (required)
  - `forDate`: Date for weather information (required, format: YYYY-MM-DD)

**Example Request:**
```
GET /weather?pincode=580004&forDate=2024-10-10
```

**Example Response:**
```json
{
    "pincode": "580004",
    "date": "2024-10-10",
    "description": "The weather in Dharwad, India, is currently scattered clouds with a temperature of 25.85°C, feeling like 26.08°C. The humidity is at 61%, and the wind is blowing at 5.14 m/s. Visibility is 6.0 km, and atmospheric pressure is 1011 hPa at sea level."
}
```

## Caching Mechanism

The application implements a two-level caching strategy:
1. Database Caching for Pincode Locations
2. Database Caching for Weather Information

## Error Handling

- 404: Pincode not found
- 500: Internal server error
- API connection issues handled gracefully

## Testing

Run tests using Maven:
```bash
mvn test
```

## Logging

The application uses SLF4J with Logback for logging. Log levels can be configured in `application.properties`:

```properties
logging.level.com.weather=DEBUG
```
