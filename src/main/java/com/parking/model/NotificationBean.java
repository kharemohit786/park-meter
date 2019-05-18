package com.parking.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String s3Url;
    private Long time;
    private String locationId;

}