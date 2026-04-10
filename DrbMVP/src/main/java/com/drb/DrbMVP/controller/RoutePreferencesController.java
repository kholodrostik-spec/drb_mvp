package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.dto.route.RoutePreferencesDto;
import com.drb.DrbMVP.service.RoutePreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route-preferences")
@Tag(name = "Route Preferences", description = "User route preference settings")
public class RoutePreferencesController {

    private final RoutePreferencesService service;

    public RoutePreferencesController(RoutePreferencesService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get route preferences for a user")
    public RoutePreferencesDto getPreferences(@RequestParam Long userId) {
        return service.getPreferences(userId);
    }

    @PutMapping
    @Operation(summary = "Update route preferences for a user")
    public RoutePreferencesDto updatePreferences(
            @RequestParam Long userId,
            @RequestBody RoutePreferencesDto dto) {
        return service.updatePreferences(userId, dto);
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset route preferences to defaults")
    public RoutePreferencesDto resetToDefault(@RequestParam Long userId) {
        return service.resetToDefault(userId);
    }
}
