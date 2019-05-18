package com.parking.service;

import com.parking.entity.SlotAvailability;
import com.parking.model.ParkingSlotRequest;
import com.parking.repository.SlotAvailabilityRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParkingFetcherImpl implements ParkingFetcher {

    private final SlotAvailabilityRepository slotAvailabilityRepository;
    
    @Override
    public ParkingSlotRequest getAvailableParking(String locationName, String vehicleType) {

        SlotAvailability slotsAvailable = slotAvailabilityRepository.findByLocationNameAndVehicleType(locationName, vehicleType);
        int usedSlots = 0;
        if(null != slotsAvailable){
            usedSlots = slotsAvailable.getUsedSlot();
        }

        return ParkingSlotRequest.builder()
                                    .slots(usedSlots)
                                    .vehicleType(vehicleType)
                                    .location(locationName)
                                    .build();
    }

}
