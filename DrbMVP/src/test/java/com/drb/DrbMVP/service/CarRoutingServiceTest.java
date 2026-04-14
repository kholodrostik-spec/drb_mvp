package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.route.AiRouteResponseDto;
import com.drb.DrbMVP.dto.route.RouteCandidate;
import com.drb.DrbMVP.repository.CarRoutingRepository;
import com.drb.DrbMVP.repository.RoutePreferencesRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CarRoutingServiceTest {

    @Mock
    private CarRoutingRepository carRepo;
    @Mock
    private RoutePreferencesRepository prefsRepo;

    @InjectMocks
    private CarRoutingService carRoutingService;

    private static final Long USER_ID = 1L;
    private static final double LAT_FROM = 53.3498;
    private static final double LON_FROM = -6.2603;
    private static final double LAT_TO   = 51.8985;
    private static final double LON_TO   = -8.4756;

    private RouteCandidate fastestCandidate;
    private RouteCandidate safestCandidate;

    @BeforeEach
    void setUp() {
        fastestCandidate = new RouteCandidate(
                "fastest", 10000.0, 15.0, 5,
                0.2, 0.1, 0.5, 0.4, 0.8, 0.0,
                "{\"type\":\"LineString\",\"coordinates\":[]}", "{}", "{}"
        );
        safestCandidate = new RouteCandidate(
                "safest", 12000.0, 18.0, 3,
                0.6, 0.2, 0.9, 0.6, 0.7, 0.0,
                "{\"type\":\"LineString\",\"coordinates\":[]}", "{}", "{}"
        );
    }

    @Test
    void findBestRoute_sourceNodeNull_throwsRuntimeException() {
        Mockito.when(carRepo.findNearestCarNode(LAT_FROM, LON_FROM)).thenReturn(null);

        Assertions.assertThrows(RuntimeException.class,
                () -> carRoutingService.findBestRoute(USER_ID, LAT_FROM, LON_FROM, LAT_TO, LON_TO));
    }

    @Test
    void findBestRoute_targetNodeNull_throwsRuntimeException() {
        Mockito.when(carRepo.findNearestCarNode(LAT_FROM, LON_FROM)).thenReturn(1L);
        Mockito.when(carRepo.findNearestCarNode(LAT_TO, LON_TO)).thenReturn(null);

        Assertions.assertThrows(RuntimeException.class,
                () -> carRoutingService.findBestRoute(USER_ID, LAT_FROM, LON_FROM, LAT_TO, LON_TO));
    }

    @Test
    void findBestRoute_noCandidates_throwsRuntimeException() {
        Mockito.when(carRepo.findNearestCarNode(LAT_FROM, LON_FROM)).thenReturn(1L);
        Mockito.when(carRepo.findNearestCarNode(LAT_TO, LON_TO)).thenReturn(2L);
        Mockito.when(carRepo.buildAllCandidates(1L, 2L, LAT_FROM, LON_FROM, LAT_TO, LON_TO))
                .thenReturn(Collections.emptyList());

        Assertions.assertThrows(RuntimeException.class,
                () -> carRoutingService.findBestRoute(USER_ID, LAT_FROM, LON_FROM, LAT_TO, LON_TO));
    }

    @Test
    void findBestRoute_pythonUnavailable_fallsBackToScoring() {
        // Python is unreachable in unit tests — service falls back to safety+beauty scoring
        Mockito.when(carRepo.findNearestCarNode(LAT_FROM, LON_FROM)).thenReturn(1L);
        Mockito.when(carRepo.findNearestCarNode(LAT_TO, LON_TO)).thenReturn(2L);
        Mockito.when(carRepo.buildAllCandidates(1L, 2L, LAT_FROM, LON_FROM, LAT_TO, LON_TO))
                .thenReturn(List.of(fastestCandidate, safestCandidate));

        AiRouteResponseDto result = carRoutingService.findBestRoute(USER_ID, LAT_FROM, LON_FROM, LAT_TO, LON_TO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("safest", result.getSelectedProfile());
        Assertions.assertNotNull(result.getSelected());
        Assertions.assertFalse(result.getAllCandidates().isEmpty());
    }

    @Test
    void findBestRoute_returnsBestRouteWithGeoJson() {
        Mockito.when(carRepo.findNearestCarNode(LAT_FROM, LON_FROM)).thenReturn(1L);
        Mockito.when(carRepo.findNearestCarNode(LAT_TO, LON_TO)).thenReturn(2L);
        Mockito.when(carRepo.buildAllCandidates(1L, 2L, LAT_FROM, LON_FROM, LAT_TO, LON_TO))
                .thenReturn(List.of(fastestCandidate));

        AiRouteResponseDto result = carRoutingService.findBestRoute(USER_ID, LAT_FROM, LON_FROM, LAT_TO, LON_TO);

        Assertions.assertNotNull(result.getSelected().getRouteGeoJson());
        Assertions.assertTrue(result.getSelected().getRouteGeoJson().contains("LineString"));
    }

    @Test
    void findBestRoute_allCandidatesIncludedInResponse() {
        Mockito.when(carRepo.findNearestCarNode(LAT_FROM, LON_FROM)).thenReturn(1L);
        Mockito.when(carRepo.findNearestCarNode(LAT_TO, LON_TO)).thenReturn(2L);
        Mockito.when(carRepo.buildAllCandidates(1L, 2L, LAT_FROM, LON_FROM, LAT_TO, LON_TO))
                .thenReturn(List.of(fastestCandidate, safestCandidate));

        AiRouteResponseDto result = carRoutingService.findBestRoute(USER_ID, LAT_FROM, LON_FROM, LAT_TO, LON_TO);

        Assertions.assertEquals(2, result.getAllCandidates().size());
    }
}
