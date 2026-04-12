package com.drb.DrbMVP.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutePreferencesDto {
    @JsonProperty("wTime")
    private double wTime;
    @JsonProperty("wSafety")
    private double wSafety;
    @JsonProperty("wSimplicity")
    private double wSimplicity;
    @JsonProperty("wBeauty")
    private double wBeauty;
    @JsonProperty("wResidential")
    private double wResidential;
    @JsonProperty("wMinor")
    private double wMinor;
}
