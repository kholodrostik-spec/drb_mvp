package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.location.NearestPointDto;
import com.drb.DrbMVP.dto.route.RouteDto;
import com.drb.DrbMVP.repository.MapRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock
    private MapRepository mapRepository;
    @Mock
    private WebClient webClient;

    @InjectMocks
    private MapService mapService;

    private static final double VALID_LAT = 53.3498;
    private static final double VALID_LON = -6.2603;

    private static final double VALID_LAT2 = 51.8985;
    private static final double VALID_LON2 = -8.4756;

    @Test
    void findNearestRoad_validCoordinates_returnsNearestPoint() {
        NearestPointDto expected = new NearestPointDto(1L, "Main St", "residential", 10.0, VALID_LON, VALID_LAT);
        Mockito.when(mapRepository.findNearestRoad(VALID_LAT, VALID_LON)).thenReturn(expected);

        NearestPointDto result = mapService.findNearestRoad(VALID_LAT, VALID_LON);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Main St", result.getName());
        Assertions.assertEquals("residential", result.getHighway());
    }

    @Test
    void findNearestRoad_outsideIreland_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> mapService.findNearestRoad(50.0, 30.0));

        Mockito.verify(mapRepository, Mockito.never()).findNearestRoad(Mockito.anyDouble(), Mockito.anyDouble());
    }

    @Test
    void findShortestRoute_validCoordinates_returnsRoute() {
        RouteDto expected = new RouteDto(1500.0, 1400.0, 50.0, 50.0,
                "{\"type\":\"LineString\"}", "{}", "{}");
        Mockito.when(mapRepository.findShortestRoute(VALID_LAT, VALID_LON, VALID_LAT2, VALID_LON2))
                .thenReturn(expected);

        RouteDto result = mapService.findShortestRoute(VALID_LAT, VALID_LON, VALID_LAT2, VALID_LON2);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1500.0, result.getTotalCost(), 0.01);
        Assertions.assertNotNull(result.getRouteGeoJson());
    }

    @Test
    void findShortestRoute_fromOutsideIreland_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> mapService.findShortestRoute(50.0, 30.0, VALID_LAT2, VALID_LON2));

        Mockito.verify(mapRepository, Mockito.never())
                .findShortestRoute(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble());
    }

    @Test
    void findShortestRoute_toOutsideIreland_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> mapService.findShortestRoute(VALID_LAT, VALID_LON, 50.0, 30.0));

        Mockito.verify(mapRepository, Mockito.never())
                .findShortestRoute(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble());
    }
}
