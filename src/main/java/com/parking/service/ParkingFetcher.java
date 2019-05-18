package com.parking.service;

import com.parking.model.ParkingSlotRequest;

public interface ParkingFetcher {
  
   ParkingSlotRequest getAvailableParking(String locationName, String vehicleType);
   
}

