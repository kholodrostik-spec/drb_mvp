package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.dto.PlaceDto;
import com.drb.DrbMVP.dto.PlaceResponseDto;
import com.drb.DrbMVP.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
@Tag(name = "Places API", description = "Endpoints for managing places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @PostMapping
    @Operation(
            summary = "Add a new place",
            description = "Saves a place with coordinates and automatically finds the nearest road"
    )
    public PlaceResponseDto addPlace(@RequestBody PlaceDto dto) {
        return placeService.save(dto);
    }
}
