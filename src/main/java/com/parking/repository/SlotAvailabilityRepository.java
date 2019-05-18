package com.parking.repository;

import com.parking.entity.SlotAvailability;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlotAvailabilityRepository extends CrudRepository<SlotAvailability, Long> {

    SlotAvailability findByLocationNameAndVehicleType(String locationName, String vehicleType);

    Optional<SlotAvailability> getByLocationName(String locationId);

}
