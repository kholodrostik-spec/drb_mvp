package com.drb.DrbMVP.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiRouteResponseDto {
    private String selectedProfile;
    private RouteCandidate selected;
    private List<RouteCandidate> allCandidates;
}
