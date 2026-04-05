package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.apilog.ApiLogDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApiLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApiLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ApiLogDto> findLatest(int limit) {
        String sql = """
            SELECT id, user_email, method, path, query_params, request_body, status, duration_ms
            FROM api_logs
            ORDER BY id DESC
            LIMIT ?
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ApiLogDto(
                rs.getLong("id"),
                rs.getString("user_email"),
                rs.getString("method"),
                rs.getString("path"),
                rs.getString("query_params"),
                rs.getString("request_body"),
                rs.getInt("status"),
                rs.getLong("duration_ms")
        ), limit);
    }

    public List<ApiLogDto> findLatestByEmail(String email, int limit) {
        String sql = """
        SELECT id, user_email, method, path, query_params, request_body, status, duration_ms
        FROM api_logs
        WHERE user_email = ?
        ORDER BY id DESC
        LIMIT ?
    """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ApiLogDto(
                rs.getLong("id"),
                rs.getString("user_email"),
                rs.getString("method"),
                rs.getString("path"),
                rs.getString("query_params"),
                rs.getString("request_body"),
                rs.getInt("status"),
                rs.getLong("duration_ms")
        ), email, limit);
    }

    public void save(String userEmail, String method, String path, String query_params, String request_body, int status, long durationMs) {
        String sql = """
            INSERT INTO api_logs (user_email, method, path, query_params, request_body, status, duration_ms)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, userEmail, method, path, query_params, request_body, status, durationMs);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM api_logs WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM api_logs");
    }
}
