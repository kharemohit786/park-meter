package com.parking.model;

import lombok.Data;

@Data
public class LicensePlateInfo {
    private String number;
    private double confidenceScore;
    private String type;
    private String location;
}
