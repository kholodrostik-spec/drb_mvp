package com.drb.DrbMVP.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> save(String email, String name, String hashedPassword) {
        String sql = """
            INSERT INTO users (email, name, password, role)
            VALUES (?, ?, ?, 'ROLE_USER')
            RETURNING id, email, name, role
        """;
        return jdbcTemplate.queryForMap(sql, email, name, hashedPassword);
    }

    public Optional<Map<String, Object>> findByEmail(String email) {
        String sql = "SELECT id, email, name, role, password FROM users WHERE email = ?";
        var results = jdbcTemplate.queryForList(sql, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public boolean blockUser(Long userId) {
        String sql = "UPDATE users SET role = 'ROLE_BLOCKED' WHERE id = ?";
        int updated = jdbcTemplate.update(sql, userId);
        return updated > 0;
    }
}
