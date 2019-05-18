package com.parking.controller;

import com.parking.model.ParkingSlotRequest;
import com.parking.service.ParkingFetcher;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping
@CrossOrigin
public class ParkingCheckController {

    private final ParkingFetcher parkingFetcher;

    @GetMapping(value = "/getParkingCount")
    public ParkingSlotRequest getParkingInfo(@RequestParam("locationName") String locationName,
                                             @RequestParam("type") String type) {
        return parkingFetcher.getAvailableParking(locationName, type);
    }

    
    @PostMapping(value="/updateTotalCount")
    public void updateTotalCount(@RequestBody ParkingSlotRequest parkingSlotRequest) {
        return ;
    }
    
   /* @GetMapping(value = "/getParkingCount/{locationId}")
    public ParkingInfo getParkingInfo(@PathVariable("locationId") int locationId) {
        return parkingFetcher.getAvailableParking(locationId);
    }*/


}
