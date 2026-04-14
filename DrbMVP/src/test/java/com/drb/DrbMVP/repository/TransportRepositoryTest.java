package com.drb.DrbMVP.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class TransportRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TransportRepository transportRepository;

    @Test
    void findNearestStops_returnsList() {
        List<Map<String, Object>> expected = List.of(
                Map.of("stop_id", "stop_001", "stop_name", "Central", "distance_m", 50.0)
        );

        Mockito.when(jdbcTemplate.queryForList(
                Mockito.anyString(),
                Mockito.eq(-6.2), Mockito.eq(53.3), Mockito.eq(-6.2), Mockito.eq(53.3)
        )).thenReturn(expected);

        List<Map<String, Object>> result = transportRepository.findNearestStops(53.3, -6.2);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("stop_001", result.get(0).get("stop_id"));
    }

    @Test
    void findStopDetails_returnsMap() {
        Map<String, Object> expected = Map.of(
                "stop_id", "stop_001",
                "stop_name", "Central",
                "latitude", 53.3,
                "longitude", -6.2
        );

        Mockito.when(jdbcTemplate.queryForMap(Mockito.anyString(), Mockito.eq("stop_001")))
                .thenReturn(expected);

        Map<String, Object> result = transportRepository.findStopDetails("stop_001");

        Assertions.assertEquals("Central", result.get("stop_name"));
    }

    @Test
    void findNextDepartures_returnsList() {
        List<Map<String, Object>> expected = List.of(
                Map.of("departure_time", "14:30:00", "trip_headsign", "City", "route_short", "10")
        );

        Mockito.when(jdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("stop_001")))
                .thenReturn(expected);

        List<Map<String, Object>> result = transportRepository.findNextDepartures("stop_001");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("10", result.get(0).get("route_short"));
    }

    @Test
    void findRouteGeoJson_returnsString() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(String.class), Mockito.eq("10"), Mockito.eq("10")))
                .thenReturn("{\"type\":\"LineString\"}");

        String result = transportRepository.findRouteGeoJson("10");

        Assertions.assertTrue(result.contains("LineString"));
    }

    @Test
    void findNearestStopOfRoute_returnsMap() {
        Map<String, Object> expected = Map.of(
                "stop_id", "stop_001",
                "stop_name", "Central",
                "distance_m", 50.0
        );

        Mockito.when(jdbcTemplate.queryForMap(
                Mockito.anyString(),
                Mockito.eq(-6.2), Mockito.eq(53.3), Mockito.eq("10"), Mockito.eq(-6.2), Mockito.eq(53.3)
        )).thenReturn(expected);

        Map<String, Object> result = transportRepository.findNearestStopOfRoute("10", 53.3, -6.2);

        Assertions.assertEquals("stop_001", result.get("stop_id"));
    }
}