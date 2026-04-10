package com.drb.DrbMVP.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutePreferencesDto {
    private double wTime;
    private double wSafety;
    private double wSimplicity;
    private double wBeauty;
    private double wResidential;
    private double wMinor;
}
