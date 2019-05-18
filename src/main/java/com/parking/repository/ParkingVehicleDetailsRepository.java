package com.parking.repository;

import com.parking.entity.ParkingVehicleDetails;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingVehicleDetailsRepository extends CrudRepository<ParkingVehicleDetails, Long> {

    Optional<ParkingVehicleDetails> getByLicensePlate(String number);

}
