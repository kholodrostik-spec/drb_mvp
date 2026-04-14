package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.route.UserRoutePreferences;
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

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RoutePreferencesRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RoutePreferencesRepository routePreferencesRepository;

    @Test
    void findByUserId_returnsPreferences() {
        UserRoutePreferences expected = new UserRoutePreferences(1L, 0.3, 0.2, 0.1, 0.2, 0.1, 0.1);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<UserRoutePreferences>>any(),
                Mockito.eq(1L)
        )).thenReturn(expected);

        Optional<UserRoutePreferences> result = routePreferencesRepository.findByUserId(1L);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(0.3, result.get().getWTime(), 0.001);
    }

    @Test
    void findByUserId_emptyResult_returnsEmpty() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<UserRoutePreferences>>any(),
                Mockito.eq(1L)
        )).thenThrow(new EmptyResultDataAccessException(1));

        Optional<UserRoutePreferences> result = routePreferencesRepository.findByUserId(1L);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void upsert_callsJdbcUpdate() {
        UserRoutePreferences prefs = new UserRoutePreferences(1L, 0.3, 0.2, 0.1, 0.2, 0.1, 0.1);

        routePreferencesRepository.upsert(prefs);

        Mockito.verify(jdbcTemplate).update(
                Mockito.anyString(),
                Mockito.eq(1L),
                Mockito.eq(0.3),
                Mockito.eq(0.2),
                Mockito.eq(0.1),
                Mockito.eq(0.2),
                Mockito.eq(0.1),
                Mockito.eq(0.1)
        );
    }

    @Test
    void resetToDefault_callsJdbcUpdate() {
        routePreferencesRepository.resetToDefault(1L);

        Mockito.verify(jdbcTemplate).update(
                Mockito.anyString(),
                Mockito.eq(1L),
                Mockito.anyDouble(),
                Mockito.anyDouble(),
                Mockito.anyDouble(),
                Mockito.anyDouble(),
                Mockito.anyDouble(),
                Mockito.anyDouble()
        );
    }
}