package com.drb.DrbMVP.service;

import com.drb.DrbMVP.repository.TransportRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock
    private TransportRepository transportRepository;

    @InjectMocks
    private TransportService transportService;

    private static final double LAT = 53.3498;
    private static final double LON = -6.2603;
    private static final String STOP_ID = "stop_001";
    private static final String ROUTE_SHORT = "10";

    @Test
    void getNearestStops_delegatesToRepository() {
        List<Map<String, Object>> expected = List.of(
                Map.of("stop_id", STOP_ID, "stop_name", "Central Stop", "distance_m", 50.0)
        );
        Mockito.when(transportRepository.findNearestStops(LAT, LON)).thenReturn(expected);

        List<Map<String, Object>> result = transportService.getNearestStops(LAT, LON);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(STOP_ID, result.get(0).get("stop_id"));
    }

    @Test
    void getNearestStops_noStops_returnsEmptyList() {
        Mockito.when(transportRepository.findNearestStops(LAT, LON)).thenReturn(List.of());

        List<Map<String, Object>> result = transportService.getNearestStops(LAT, LON);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getStopWithDepartures_containsStopAndDepartures() {
        Map<String, Object> stopDetails = Map.of("stop_id", STOP_ID, "stop_name", "Central Stop");
        List<Map<String, Object>> departures = List.of(
                Map.of("departure_time", "14:30:00", "route_short", ROUTE_SHORT)
        );

        Mockito.when(transportRepository.findStopDetails(STOP_ID)).thenReturn(stopDetails);
        Mockito.when(transportRepository.findNextDepartures(STOP_ID)).thenReturn(departures);

        Map<String, Object> result = transportService.getStopWithDepartures(STOP_ID);

        Assertions.assertTrue(result.containsKey("stop"));
        Assertions.assertTrue(result.containsKey("next_departures"));
        Assertions.assertEquals(stopDetails, result.get("stop"));
        Assertions.assertEquals(departures, result.get("next_departures"));
    }

    @Test
    void getStopWithDepartures_noDepartures_returnsEmptyDeparturesList() {
        Mockito.when(transportRepository.findStopDetails(STOP_ID))
                .thenReturn(Map.of("stop_id", STOP_ID));
        Mockito.when(transportRepository.findNextDepartures(STOP_ID)).thenReturn(List.of());

        Map<String, Object> result = transportService.getStopWithDepartures(STOP_ID);

        Assertions.assertTrue(((List<?>) result.get("next_departures")).isEmpty());
    }

    @Test
    void getRouteWithNearestStop_containsRouteStopsAndNearestStop() {
        String geoJson = "{\"type\":\"LineString\",\"coordinates\":[]}";
        Map<String, Object> nearestStop = Map.of("stop_id", STOP_ID, "distance_m", 30.0);

        Mockito.when(transportRepository.findRouteGeoJson(ROUTE_SHORT)).thenReturn(geoJson);
        Mockito.when(transportRepository.findNearestStopOfRoute(ROUTE_SHORT, LAT, LON)).thenReturn(nearestStop);

        Map<String, Object> result = transportService.getRouteWithNearestStop(ROUTE_SHORT, LAT, LON);

        Assertions.assertTrue(result.containsKey("route_stops"));
        Assertions.assertTrue(result.containsKey("nearest_stop"));
        Assertions.assertEquals(geoJson, result.get("route_stops"));
        Assertions.assertEquals(nearestStop, result.get("nearest_stop"));
    }
}
