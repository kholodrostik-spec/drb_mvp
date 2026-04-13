package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.route.AiRouteResponseDto;
import com.drb.DrbMVP.dto.route.RouteCandidate;
import com.drb.DrbMVP.repository.CarRoutingRepository;
import com.drb.DrbMVP.repository.RoutePreferencesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Slf4j
@Service
public class CarRoutingService {

    private final CarRoutingRepository carRepo;
    private final RoutePreferencesRepository prefsRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PYTHON_URL = "http://localhost:8000/api/routes/select";

    public CarRoutingService(CarRoutingRepository carRepo,
                             RoutePreferencesRepository prefsRepo) {
        this.carRepo = carRepo;
        this.prefsRepo = prefsRepo;
    }

    public AiRouteResponseDto findBestRoute(
            Long userId,
            double latFrom, double lonFrom,
            double latTo, double lonTo) {

        Long sourceNode = carRepo.findNearestCarNode(latFrom, lonFrom);
        Long targetNode = carRepo.findNearestCarNode(latTo, lonTo);

        if (sourceNode == null || targetNode == null) {
            throw new RuntimeException("Cannot find routable nodes near given coordinates");
        }

        List<RouteCandidate> candidates = carRepo.buildAllCandidates(
                sourceNode, targetNode, latFrom, lonFrom, latTo, lonTo);

        if (candidates.isEmpty()) {
            throw new RuntimeException("No car routes found between given points");
        }

        List<Map<String, Object>> metrics = candidates.stream().map(c -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("profile",          c.getProfile());
            m.put("totalM",           c.getTotalCostM());
            m.put("timeMin",          c.getTimeMin());
            m.put("safetyScore",      c.getSafetyScore());
            m.put("beautyScore",      c.getBeautyScore());
            m.put("simplicityScore",  c.getSimplicityScore());
            m.put("residentialRatio", c.getResidentialRatio());
            m.put("minorRatio",       c.getMinorRatio());
            return m;
        }).collect(java.util.stream.Collectors.toList());

        String selectedProfile = callPython(userId, metrics);

        RouteCandidate best = candidates.stream()
                .filter(c -> c.getProfile().equals(selectedProfile))
                .findFirst()
                .orElse(candidates.get(0));

        log.info("Python selected profile: {}", selectedProfile);

        return new AiRouteResponseDto(best.getProfile(), best, candidates);
    }

    private String callPython(Long userId, List<Map<String, Object>> metrics) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("user_id", userId);
            body.put("candidates", metrics);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PYTHON_URL, request, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("selected_profile")) {
                return (String) response.getBody().get("selected_profile");
            }
        } catch (Exception e) {
            log.warn("Python ML unavailable, falling back to scoring: {}", e.getMessage());
        }

        return metrics.stream()
                .max(Comparator.comparingDouble(m ->
                        (double) m.get("safetyScore") + (double) m.get("beautyScore")))
                .map(m -> (String) m.get("profile"))
                .orElse("balanced");
    }
}