package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.route.UserRoutePreferences;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoutePreferencesRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoutePreferencesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserRoutePreferences> findByUserId(Long userId) {
        String sql = """
            SELECT user_id, w_time, w_safety, w_simplicity,
                   w_beauty, w_residential, w_minor
            FROM user_route_preferences
            WHERE user_id = ?
        """;
        try {
            UserRoutePreferences prefs = jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new UserRoutePreferences(
                            rs.getLong("user_id"),
                            rs.getDouble("w_time"),
                            rs.getDouble("w_safety"),
                            rs.getDouble("w_simplicity"),
                            rs.getDouble("w_beauty"),
                            rs.getDouble("w_residential"),
                            rs.getDouble("w_minor")
                    ), userId);
            return Optional.ofNullable(prefs);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void upsert(UserRoutePreferences prefs) {
        String sql = """
            INSERT INTO user_route_preferences
                (user_id, w_time, w_safety, w_simplicity, w_beauty, w_residential, w_minor, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
            ON CONFLICT (user_id) DO UPDATE SET
                w_time        = EXCLUDED.w_time,
                w_safety      = EXCLUDED.w_safety,
                w_simplicity  = EXCLUDED.w_simplicity,
                w_beauty      = EXCLUDED.w_beauty,
                w_residential = EXCLUDED.w_residential,
                w_minor       = EXCLUDED.w_minor,
                updated_at    = NOW()
        """;
        jdbcTemplate.update(sql,
                prefs.getUserId(),
                prefs.getWTime(),
                prefs.getWSafety(),
                prefs.getWSimplicity(),
                prefs.getWBeauty(),
                prefs.getWResidential(),
                prefs.getWMinor()
        );
    }

    public void resetToDefault(Long userId) {
        upsert(UserRoutePreferences.defaults(userId));
    }
}