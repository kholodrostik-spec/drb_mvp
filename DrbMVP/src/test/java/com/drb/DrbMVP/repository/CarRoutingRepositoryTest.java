package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.route.RouteCandidate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CarRoutingRepositoryTest {

    private final JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);

    @Test
    void findNearestCarNode_returnsId() {
        CarRoutingRepository repository = new CarRoutingRepository(jdbcTemplate);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.eq(Long.class),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        )).thenReturn(123L);

        Long result = repository.findNearestCarNode(53.3, -6.2);

        Assertions.assertEquals(123L, result);
    }

    @Test
    void findNearestCarNode_emptyResult_returnsNull() {
        CarRoutingRepository repository = new CarRoutingRepository(jdbcTemplate);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.eq(Long.class),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        )).thenThrow(new EmptyResultDataAccessException(1));

        Long result = repository.findNearestCarNode(53.3, -6.2);

        Assertions.assertNull(result);
    }

    @Test
    void buildCandidate_returnsMappedCandidate() throws Exception {
        CarRoutingRepository repository = new CarRoutingRepository(jdbcTemplate);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<RouteCandidate>>any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any()
        )).thenAnswer(invocation -> {
            RowMapper<RouteCandidate> mapper = invocation.getArgument(1);

            ResultSet rs = Mockito.mock(ResultSet.class);
            Mockito.when(rs.getDouble("total_m")).thenReturn(1000.0);
            Mockito.when(rs.getDouble("residential_m")).thenReturn(300.0);
            Mockito.when(rs.getDouble("minor_m")).thenReturn(200.0);
            Mockito.when(rs.getInt("turn_count")).thenReturn(2);
            Mockito.when(rs.getInt("segment_count")).thenReturn(10);
            Mockito.when(rs.getString("route_geojson")).thenReturn("{\"type\":\"LineString\",\"coordinates\":[]}");
            Mockito.when(rs.getString("snap_start_geojson")).thenReturn("{}");
            Mockito.when(rs.getString("snap_end_geojson")).thenReturn("{}");

            return mapper.mapRow(rs, 0);
        });

        RouteCandidate result = repository.buildCandidate(
                "fastest", 1L, 2L,
                53.3, -6.2, 53.4, -6.1,
                "cost", "reverse_cost"
        );

        Assertions.assertNotNull(result);
        Assertions.assertEquals("fastest", result.getProfile());
        Assertions.assertEquals(1000.0, result.getTotalCostM(), 0.001);
        Assertions.assertTrue(result.getSafetyScore() >= 0 && result.getSafetyScore() <= 1);
    }

    @Test
    void buildCandidate_emptyResult_returnsNull() {
        CarRoutingRepository repository = new CarRoutingRepository(jdbcTemplate);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<RouteCandidate>>any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any()
        )).thenThrow(new EmptyResultDataAccessException(1));

        RouteCandidate result = repository.buildCandidate(
                "fastest", 1L, 2L,
                53.3, -6.2, 53.4, -6.1,
                "cost", "reverse_cost"
        );

        Assertions.assertNull(result);
    }

    @Test
    void buildAllCandidates_collectsNonNullCandidates() {
        CarRoutingRepository repository = Mockito.spy(new CarRoutingRepository(jdbcTemplate));

        RouteCandidate fastest = new RouteCandidate("fastest", 1000, 10, 2, 0.1, 0.2, 0.3, 0.4, 0.5, 0, "{}", "{}", "{}");
        RouteCandidate safest = new RouteCandidate("safest", 1200, 12, 3, 0.2, 0.3, 0.7, 0.5, 0.4, 0, "{}", "{}", "{}");
        RouteCandidate simplest = new RouteCandidate("simplest", 900, 9, 1, 0.1, 0.1, 0.4, 0.3, 0.9, 0, "{}", "{}", "{}");
        RouteCandidate scenic = new RouteCandidate("scenic", 1300, 13, 4, 0.3, 0.4, 0.5, 0.8, 0.3, 0, "{}", "{}", "{}");
        RouteCandidate balanced = new RouteCandidate("balanced", 1100, 11, 2, 0.2, 0.2, 0.6, 0.6, 0.6, 0, "{}", "{}", "{}");

        Mockito.doReturn(fastest).when(repository).buildCandidate(Mockito.eq("fastest"), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(safest).when(repository).buildCandidate(Mockito.eq("safest"), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(simplest).when(repository).buildCandidate(Mockito.eq("simplest"), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(scenic).when(repository).buildCandidate(Mockito.eq("scenic"), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(balanced).when(repository).buildCandidate(Mockito.eq("balanced"), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyString(), Mockito.anyString());

        List<RouteCandidate> result = repository.buildAllCandidates(1L, 2L, 53.3, -6.2, 53.4, -6.1);

        Assertions.assertEquals(5, result.size());
    }

    @Test
    void buildMetricsOnly_returnsMetricsMaps() {
        CarRoutingRepository repository = Mockito.spy(new CarRoutingRepository(jdbcTemplate));

        List<RouteCandidate> candidates = List.of(
                new RouteCandidate("fastest", 1000, 10, 2, 0.1, 0.2, 0.3, 0.4, 0.5, 0, "{}", "{}", "{}")
        );

        Mockito.doReturn(candidates).when(repository)
                .buildAllCandidates(1L, 2L, 53.3, -6.2, 53.4, -6.1);

        List<Map<String, Object>> result = repository.buildMetricsOnly(1L, 2L, 53.3, -6.2, 53.4, -6.1);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("fastest", result.get(0).get("profile"));
        Assertions.assertEquals(1000.0, result.get(0).get("totalM"));
    }
}