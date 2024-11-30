package com.weather.service;

import com.weather.model.PincodeLocation;

public interface GeoCodingService {
    PincodeLocation getLocationByPincode(String pincode);
}