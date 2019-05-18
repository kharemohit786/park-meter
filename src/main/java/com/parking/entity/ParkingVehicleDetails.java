package com.parking.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The persistent class for the rss_feeds database table.
 */
@Entity
@NoArgsConstructor
@Data
public class ParkingVehicleDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long slotAvailibilityId;

    private String licensePlate;

    private String userid;
 
    private Boolean isParked;

    private Long createdDbTime;
    
    private Long lastModifiedDbTime;
}