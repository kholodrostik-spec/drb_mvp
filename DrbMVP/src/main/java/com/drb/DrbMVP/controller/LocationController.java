package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.dto.location.LocationDto;
import com.drb.DrbMVP.dto.location.LocationResponseDto;
import com.drb.DrbMVP.dto.review.ReviewDto;
import com.drb.DrbMVP.dto.review.ReviewResponseDto;
import com.drb.DrbMVP.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations API", description = "Endpoints for managing locations and reviews")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @Operation(
            summary = "Add a new location",
            description = "Saves a new location and automatically finds the nearest road"
    )
    public LocationResponseDto addLocation(@RequestBody LocationDto dto) {
        return locationService.addLocation(dto);
    }

    @PostMapping("/reviews")
    @Operation(
            summary = "Add or update a review",
            description = "Adds a rating and comment to a location. One review per user per location."
    )
    public ReviewResponseDto addReview(@RequestBody ReviewDto dto) {
        return locationService.addReview(dto);
    }
}
