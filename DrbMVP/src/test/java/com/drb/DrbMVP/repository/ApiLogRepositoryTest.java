package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.apilog.ApiLogDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ApiLogRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ApiLogRepository apiLogRepository;

    @Test
    void findLatest_returnsLogs() {
        List<ApiLogDto> expected = List.of(
                new ApiLogDto(1L, "test@example.com", "GET", "/api/test", "a=1", "{\"x\":1}", 200, 42L)
        );

        Mockito.when(jdbcTemplate.query(
                Mockito.anyString(),
                Mockito.<RowMapper<ApiLogDto>>any(),
                Mockito.eq(10)
        )).thenReturn(expected);

        List<ApiLogDto> result = apiLogRepository.findLatest(10);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("test@example.com", result.get(0).getUserEmail());
        Assertions.assertEquals("GET", result.get(0).getMethod());
    }

    @Test
    void findLatestByEmail_returnsLogs() {
        List<ApiLogDto> expected = List.of(
                new ApiLogDto(2L, "me@example.com", "POST", "/api/me", null, null, 201, 30L)
        );

        Mockito.when(jdbcTemplate.query(
                Mockito.anyString(),
                Mockito.<RowMapper<ApiLogDto>>any(),
                Mockito.eq("me@example.com"),
                Mockito.eq(5)
        )).thenReturn(expected);

        List<ApiLogDto> result = apiLogRepository.findLatestByEmail("me@example.com", 5);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("me@example.com", result.get(0).getUserEmail());
    }

    @Test
    void save_callsJdbcUpdate() {
        apiLogRepository.save("test@example.com", "GET", "/api/test", "a=1", "{\"x\":1}", 200, 42L);

        Mockito.verify(jdbcTemplate).update(
                Mockito.anyString(),
                Mockito.eq("test@example.com"),
                Mockito.eq("GET"),
                Mockito.eq("/api/test"),
                Mockito.eq("a=1"),
                Mockito.eq("{\"x\":1}"),
                Mockito.eq(200),
                Mockito.eq(42L)
        );
    }

    @Test
    void deleteById_callsJdbcUpdate() {
        apiLogRepository.deleteById(7L);
        Mockito.verify(jdbcTemplate).update(Mockito.anyString(), Mockito.eq(7L));
    }

    @Test
    void deleteAll_callsJdbcUpdate() {
        apiLogRepository.deleteAll();
        Mockito.verify(jdbcTemplate).update("DELETE FROM api_logs");
    }
}