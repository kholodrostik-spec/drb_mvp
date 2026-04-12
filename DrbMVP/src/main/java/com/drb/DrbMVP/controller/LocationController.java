package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.dto.location.LocationDto;
import com.drb.DrbMVP.dto.location.LocationResponseDto;
import com.drb.DrbMVP.dto.location.ReviewCheckDto;
import com.drb.DrbMVP.dto.review.ReviewDto;
import com.drb.DrbMVP.dto.review.ReviewResponseDto;
import com.drb.DrbMVP.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations API", description = "Endpoints for managing locations and reviews")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/nearest")
    @Operation(summary = "Find nearest location")
    public LocationResponseDto findNearestLocation(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon
    ) {
        return locationService.findNearestLocation(lat, lon);
    }

    @GetMapping("/review-check")
    @Operation(
            summary = "Check if user already reviewed the nearest location",
            description = "Finds nearest location to coordinates, then checks if the user has an existing review. "
                    + "Returns hasReview=true with existing rating/comment if found."
    )
    public ReviewCheckDto checkExistingReview(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("userId") Long userId
    ) {
        return locationService.checkExistingReview(lat, lon, userId);
    }

    @GetMapping("/search")
    @Operation(summary = "Search locations by name")
    public List<LocationResponseDto> searchLocations(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        return locationService.searchByName(query, limit);
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
    public ReviewResponseDto addReview(
            @RequestParam("locationId") Long locationId,
            @RequestParam("userId") Long userId,
            @RequestParam("rating") Double rating,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {

        log.info("Photo received: {}", photo != null ? photo.getOriginalFilename() + " size=" + photo.getSize() : "NULL");

        ReviewDto dto = new ReviewDto();
        dto.setLocationId(locationId);
        dto.setUserId(userId);
        dto.setRating(rating);
        dto.setComment(comment);

        return locationService.addReview(dto, photo);
    }

    @DeleteMapping("/reviews/photo")
    @Operation(
            summary = "Delete review photo",
            description = "Deletes photo from S3 and cleans up link in review"
    )
    public ResponseEntity<Void> deleteReviewPhoto(
            @RequestParam Long locationId,
            @RequestParam Long userId
    ) {
        locationService.deleteReviewPhoto(locationId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/comment")
    @Operation(summary = "Delete review comment")
    public ResponseEntity<Void> deleteReviewComment(
            @RequestParam Long locationId,
            @RequestParam Long userId
    ) {
        locationService.deleteReviewComment(locationId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/rating")
    @Operation(summary = "Delete review rating")
    public ResponseEntity<Void> deleteReviewRating(
            @RequestParam Long locationId,
            @RequestParam Long userId
    ) {
        locationService.deleteReviewRating(locationId, userId);
        return ResponseEntity.noContent().build();
    }
}
