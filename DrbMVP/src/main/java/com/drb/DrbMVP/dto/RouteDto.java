package com.drb.DrbMVP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RouteDto {
    private Double totalCost;
    private Double roadCost;
    private Double snapStartCost;
    private Double snapEndCost;
    private String routeGeoJson;
    private String snapStartGeoJson;
    private String snapEndGeoJson;
}
