package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.route.RoutePreferencesDto;
import com.drb.DrbMVP.dto.route.UserRoutePreferences;
import com.drb.DrbMVP.repository.RoutePreferencesRepository;
import org.springframework.stereotype.Service;

@Service
public class RoutePreferencesService {

    private final RoutePreferencesRepository repo;

    public RoutePreferencesService(RoutePreferencesRepository repo) {
        this.repo = repo;
    }

    public RoutePreferencesDto getPreferences(Long userId) {
        UserRoutePreferences prefs = repo.findByUserId(userId)
                .orElse(UserRoutePreferences.defaults(userId));
        return toDto(prefs);
    }

    public RoutePreferencesDto updatePreferences(Long userId, RoutePreferencesDto dto) {
        UserRoutePreferences prefs = new UserRoutePreferences(
                userId,
                dto.getWTime(),
                dto.getWSafety(),
                dto.getWSimplicity(),
                dto.getWBeauty(),
                dto.getWResidential(),
                dto.getWMinor()
        );
        repo.upsert(prefs);
        return toDto(prefs);
    }

    public RoutePreferencesDto resetToDefault(Long userId) {
        repo.resetToDefault(userId);
        return toDto(UserRoutePreferences.defaults(userId));
    }

    private RoutePreferencesDto toDto(UserRoutePreferences p) {
        return new RoutePreferencesDto(
                p.getWTime(), p.getWSafety(), p.getWSimplicity(),
                p.getWBeauty(), p.getWResidential(), p.getWMinor()
        );
    }
}
