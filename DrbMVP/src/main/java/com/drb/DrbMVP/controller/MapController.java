package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.dto.location.NearestPointDto;
import com.drb.DrbMVP.dto.route.AiRouteResponseDto;
import com.drb.DrbMVP.dto.route.FastestRouteResponseDto;
import com.drb.DrbMVP.dto.route.RouteDto;
import com.drb.DrbMVP.service.CarRoutingService;
import com.drb.DrbMVP.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
@Tag(name = "Map API", description = "Endpoints for map operations")
public class MapController {

    private final MapService mapService;
    private final CarRoutingService carRoutingService;

    public MapController(MapService mapService, CarRoutingService carRoutingService) {
        this.mapService = mapService;
        this.carRoutingService = carRoutingService;
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

    @GetMapping("/route")
    @Operation(
            summary = "Find shortest route between two points",
            description = "Returns shortest path along roads using Dijkstra algorithm"
    )
    public RouteDto findRoute(
            @Parameter(description = "Latitude of start point", example = "53.2707")
            @RequestParam double latFrom,
            @Parameter(description = "Longitude of start point", example = "-9.0568")
            @RequestParam double lonFrom,
            @Parameter(description = "Latitude of end point", example = "53.3498")
            @RequestParam double latTo,
            @Parameter(description = "Longitude of end point", example = "-6.2603")
            @RequestParam double lonTo) {
        return mapService.findShortestRoute(latFrom, lonFrom, latTo, lonTo);
    }

    @GetMapping("/ai-route")
    @Operation(
            summary = "Find personalized car route",
            description = "Builds multiple candidate routes and selects the best one based on user preferences"
    )
    public AiRouteResponseDto getAiRoute(
            @Parameter(description = "User ID for loading preferences", example = "1")
            @RequestParam Long userId,
            @Parameter(example = "53.2707") @RequestParam double latFrom,
            @Parameter(example = "-9.0568") @RequestParam double lonFrom,
            @Parameter(example = "53.3498") @RequestParam double latTo,
            @Parameter(example = "-6.2603") @RequestParam double lonTo) {
        return carRoutingService.findBestRoute(userId, latFrom, lonFrom, latTo, lonTo);
    }
}
