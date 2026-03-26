package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.dto.location.LocationDto;
import com.drb.DrbMVP.dto.location.LocationResponseDto;
import com.drb.DrbMVP.dto.review.ReviewDto;
import com.drb.DrbMVP.dto.review.ReviewResponseDto;
import com.drb.DrbMVP.dto.review.ReviewWithPhotoRequest;
import com.drb.DrbMVP.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add or update a review",
            description = "Adds a rating and comment to a location. One review per user per location.")
//    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
//            schema = @Schema(implementation = ReviewWithPhotoRequest.class)))
    public ReviewResponseDto addReview(
            @RequestParam Long locationId,
            @RequestParam Long userId,
            @RequestParam Double rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) MultipartFile photo
    ) {
        ReviewDto dto = new ReviewDto();
        dto.setLocationId(locationId);
        dto.setUserId(userId);
        dto.setRating(rating);
        dto.setComment(comment);

        return locationService.addReview(dto, photo);
    }
}
