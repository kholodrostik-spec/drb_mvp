package com.drb.DrbMVP.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FastestRouteResponseDto {
    private List<List<Double>> path;
    private Double time_min;
    private Double distance_m;
}
