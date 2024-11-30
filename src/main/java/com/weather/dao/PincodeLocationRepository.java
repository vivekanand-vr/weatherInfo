package com.weather.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.weather.model.PincodeLocation;

public interface PincodeLocationRepository extends JpaRepository<PincodeLocation, Long> {
    Optional<PincodeLocation> findByPincode(String pincode);
}