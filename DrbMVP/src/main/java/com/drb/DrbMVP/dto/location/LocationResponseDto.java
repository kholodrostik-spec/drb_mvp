package com.drb.DrbMVP.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponseDto {
    private Long id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String category;
    private Boolean isDefault;
    private String nearestRoadName;
    private String nearestRoadHighway;
    private Double nearestRoadDistance;
}
