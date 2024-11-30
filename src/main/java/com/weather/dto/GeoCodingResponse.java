package com.weather.dto;

import lombok.Data;

@Data
public class GeoCodingResponse {
    private String zip;
    private String name;
    private Double lat;
    private Double lon;
    private String country;
}