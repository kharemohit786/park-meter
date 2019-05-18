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
public class SlotAvailability {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    private String locationName;

    private int usedSlot;
    
    private String vehicleType;
    

}