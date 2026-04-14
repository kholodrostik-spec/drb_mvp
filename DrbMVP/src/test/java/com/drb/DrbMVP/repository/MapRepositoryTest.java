package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.location.NearestPointDto;
import com.drb.DrbMVP.dto.route.RouteDto;
import com.drb.DrbMVP.exception.RouteNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class MapRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private MapRepository mapRepository;

    @Test
    void findNearestRoad_returnsDto() {
        NearestPointDto expected = new NearestPointDto(1L, "Main St", "residential", 10.0, -6.2, 53.3);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<NearestPointDto>>any(),
                Mockito.eq(-6.2), Mockito.eq(53.3)
        )).thenReturn(expected);

        NearestPointDto result = mapRepository.findNearestRoad(53.3, -6.2);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Main St", result.getName());
    }

    @Test
    void findShortestRoute_returnsRoute() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.eq(Long.class),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        )).thenReturn(10L, 20L);

        RouteDto expected = new RouteDto(
                1200.0, 1000.0, 100.0, 100.0,
                "{\"type\":\"LineString\"}", "{}", "{}"
        );

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<RouteDto>>any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        )).thenReturn(expected);

        RouteDto result = mapRepository.findShortestRoute(53.3, -6.2, 53.4, -6.1);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1200.0, result.getTotalCost(), 0.001);
    }

    @Test
    void findShortestRoute_emptyResult_throwsRouteNotFoundException() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.eq(Long.class),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        )).thenReturn(10L, 20L);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<RouteDto>>any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        )).thenThrow(new EmptyResultDataAccessException(1));

        Assertions.assertThrows(RouteNotFoundException.class,
                () -> mapRepository.findShortestRoute(53.3, -6.2, 53.4, -6.1));
    }
}