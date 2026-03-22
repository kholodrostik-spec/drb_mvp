package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.service.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transport")
@Tag(name = "Transport API", description = "Public transport endpoints")
public class TransportController {

    private final TransportService transportService;

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @GetMapping("/stops/nearest")
    @Operation(
            summary = "Find 3 nearest stops",
            description = "Returns 3 nearest transport stops to given coordinates"
    )
    public List<Map<String, Object>> getNearestStops(
            @Parameter(example = "53.2743") @RequestParam double lat,
            @Parameter(example = "-9.0488") @RequestParam double lon) {
        return transportService.getNearestStops(lat, lon);
    }

    @GetMapping("/stops/{stopId}")
    @Operation(
            summary = "Get stop details with next 5 departures",
            description = "Returns stop info and next 5 departures from current time"
    )
    public Map<String, Object> getStopDetails(
            @Parameter(example = "8250DB000014") @PathVariable String stopId) {
        return transportService.getStopWithDepartures(stopId);
    }

    @GetMapping("/route")
    @Operation(
            summary = "Get route GeoJSON and nearest stop",
            description = "Returns full route as GeoJSON LineString and nearest stop to given coordinates"
    )
    public Map<String, Object> getRoute(
            @Parameter(description = "Route short name", example = "40")
            @RequestParam String routeShort,
            @Parameter(description = "Latitude", example = "53.2743")
            @RequestParam double lat,
            @Parameter(description = "Longitude", example = "-9.0488")
            @RequestParam double lon) {
        return transportService.getRouteWithNearestStop(routeShort, lat, lon);
    }
}
