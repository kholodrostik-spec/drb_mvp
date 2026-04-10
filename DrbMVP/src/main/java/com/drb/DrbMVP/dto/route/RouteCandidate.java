package com.drb.DrbMVP.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RouteCandidate {
    private String profile;
    private double totalCostM;
    private double timeMin;
    private int turnCount;
    private double residentialRatio;
    private double minorRatio;
    private double safetyScore;
    private double beautyScore;
    private double simplicityScore;
    private double totalScore;
    private String routeGeoJson;
    private String snapStartGeoJson;
    private String snapEndGeoJson;
}
