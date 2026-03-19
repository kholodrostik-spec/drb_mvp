package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.dto.NearestPointDto;
import com.drb.DrbMVP.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
@Tag(name = "Map API", description = "Endpoints for map operations")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/nearest")
    @Operation(
            summary = "Find nearest road to a point",
            description = "Returns the closest road segment to the given coordinates"
    )
    public NearestPointDto findNearest(
            @Parameter(description = "Latitude of the point", example = "53.2707")
            @RequestParam double lat,
            @Parameter(description = "Longitude of the point", example = "-9.0568")
            @RequestParam double lon) {
        return mapService.findNearestRoad(lat, lon);
    }
}
