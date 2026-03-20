package com.drb.DrbMVP.dto;

import lombok.Data;

@Data
public class PlaceDto {
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String category;
    private Double rating;
}
