package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.route.AiRouteResponseDto;
import com.drb.DrbMVP.dto.route.RouteCandidate;
import com.drb.DrbMVP.dto.route.UserRoutePreferences;
import com.drb.DrbMVP.repository.CarRoutingRepository;
import com.drb.DrbMVP.repository.RoutePreferencesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class CarRoutingService {

    private final CarRoutingRepository carRepo;
    private final RoutePreferencesRepository prefsRepo;

    public CarRoutingService(CarRoutingRepository carRepo,
                             RoutePreferencesRepository prefsRepo) {
        this.carRepo = carRepo;
        this.prefsRepo = prefsRepo;
    }

    public AiRouteResponseDto findBestRoute(
            Long userId,
            double latFrom, double lonFrom,
            double latTo, double lonTo) {

        // Load user preferences or use defaults
        UserRoutePreferences prefs = prefsRepo.findByUserId(userId)
                .orElse(UserRoutePreferences.defaults(userId));

        // Find nearest routable nodes
        Long sourceNode = carRepo.findNearestCarNode(latFrom, lonFrom);
        Long targetNode = carRepo.findNearestCarNode(latTo, lonTo);

        if (sourceNode == null || targetNode == null) {
            throw new RuntimeException("Cannot find routable nodes near given coordinates");
        }

        log.info("Car routing: source={} target={}", sourceNode, targetNode);

        // Build all candidate routes
        List<RouteCandidate> candidates = carRepo.buildAllCandidates(
                sourceNode, targetNode, latFrom, lonFrom, latTo, lonTo);

        if (candidates.isEmpty()) {
            throw new RuntimeException("No car routes found between given points");
        }

        // Score each candidate based on user preferences
        double maxTime = candidates.stream().mapToDouble(RouteCandidate::getTimeMin).max().orElse(1);
        double minTime = candidates.stream().mapToDouble(RouteCandidate::getTimeMin).min().orElse(0);
        double timeRange = maxTime - minTime > 0 ? maxTime - minTime : 1;

        candidates.forEach(c -> {
            // Normalize time to 0-1 (lower time = higher score)
            double timeScore = 1.0 - (c.getTimeMin() - minTime) / timeRange;

            double score =
                    prefs.getWTime()        * timeScore +
                            prefs.getWSafety()      * c.getSafetyScore() +
                            prefs.getWSimplicity()  * c.getSimplicityScore() +
                            prefs.getWBeauty()      * c.getBeautyScore() +
                            prefs.getWResidential() * c.getResidentialRatio() +
                            prefs.getWMinor()       * c.getMinorRatio();

            c.setTotalScore(score);
        });

        // Pick best candidate
        RouteCandidate best = candidates.stream()
                .max(Comparator.comparingDouble(RouteCandidate::getTotalScore))
                .orElseThrow();

        log.info("Selected profile: {} with score: {}", best.getProfile(), best.getTotalScore());

        return new AiRouteResponseDto(best.getProfile(), best, candidates);
    }
}
