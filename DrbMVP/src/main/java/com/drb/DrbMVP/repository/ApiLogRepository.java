package com.drb.DrbMVP.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ApiLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApiLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String userEmail, String method, String path, String query_params, String request_body, int status, long durationMs) {
        String sql = """
            INSERT INTO api_logs (user_email, method, path, query_params, request_body, status, duration_ms)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, userEmail, method, path, query_params, request_body, status, durationMs);
    }
}
