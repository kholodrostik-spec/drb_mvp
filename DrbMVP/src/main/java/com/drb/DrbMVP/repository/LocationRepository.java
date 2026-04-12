package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.location.LocationDto;
import com.drb.DrbMVP.dto.location.LocationResponseDto;
import com.drb.DrbMVP.dto.location.NearestPointDto;
import com.drb.DrbMVP.dto.location.ReviewCheckResult;
import com.drb.DrbMVP.dto.review.ReviewDto;
import com.drb.DrbMVP.dto.review.ReviewResponseDto;
import com.drb.DrbMVP.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class LocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public LocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public LocationResponseDto saveLocation(LocationDto dto, NearestPointDto nearest) {
        String sql = """
            INSERT INTO locations (
                name, description, category,
                latitude, longitude, geom,
                nearest_road_name, nearest_road_highway, nearest_road_distance,
                is_default
            ) VALUES (?, ?, ?, ?, ?,
                ST_SetSRID(ST_MakePoint(?, ?), 4326),
                ?, ?, ?, false
            )
            RETURNING id, name, description, latitude, longitude,
                      category, is_default,
                      nearest_road_name, nearest_road_highway, nearest_road_distance
        """;

        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new LocationResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("category"),
                        rs.getBoolean("is_default"),
                        rs.getString("nearest_road_name"),
                        rs.getString("nearest_road_highway"),
                        rs.getDouble("nearest_road_distance")
                ),
                dto.getName(), dto.getDescription(), dto.getCategory(),
                dto.getLatitude(), dto.getLongitude(),
                dto.getLongitude(), dto.getLatitude(),
                nearest.getName(), nearest.getHighway(), nearest.getDistance()
        );
    }

    public ReviewResponseDto saveReview(ReviewDto dto, S3Service s3Service) {
        String sql = """
            INSERT INTO reviews (location_id, user_id, rating, comment, photo_s3_key)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (location_id, user_id)
            DO UPDATE SET rating = EXCLUDED.rating,
                          comment = EXCLUDED.comment,
                          photo_s3_key   = EXCLUDED.photo_s3_key
            RETURNING location_id, user_id, rating, comment, created_at, photo_s3_key
        """;

        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> {
                    String key = rs.getString("photo_s3_key");
                    String url = key != null ? s3Service.buildUrl(key) : null;
                    return new ReviewResponseDto(
                            rs.getLong("location_id"),
                            rs.getLong("user_id"),
                            rs.getDouble("rating"),
                            rs.getString("comment"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            url
                    );
                },
                dto.getLocationId(), dto.getUserId(), dto.getRating(), dto.getComment(), dto.getPhotoS3Key()
        );
    }

    public boolean existsUser(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    public boolean existsLocation(Long locationId) {
        String sql = "SELECT COUNT(*) FROM locations WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, locationId);
        return count != null && count > 0;
    }

    public String getPhotoS3Key(Long locationId, Long userId) {
        String sql = "SELECT photo_s3_key FROM reviews WHERE location_id = ? AND user_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, locationId, userId);
    }

    public void clearPhoto(Long locationId, Long userId) {
        String sql = """
        UPDATE reviews SET photo_s3_key = NULL
        WHERE location_id = ? AND user_id = ?
    """;
        jdbcTemplate.update(sql, locationId, userId);
    }

    public void clearComment(Long locationId, Long userId) {
        String sql = """
        UPDATE reviews SET comment = NULL
        WHERE location_id = ? AND user_id = ?
    """;
        jdbcTemplate.update(sql, locationId, userId);
    }

    public void clearRating(Long locationId, Long userId) {
        String sql = """
        UPDATE reviews SET rating = NULL
        WHERE location_id = ? AND user_id = ?
    """;
        jdbcTemplate.update(sql, locationId, userId);
    }

    public LocationResponseDto findNearestLocation(double lat, double lon) {
        String sql = """
        SELECT
            l.id,
            l.name,
            l.description,
            l.latitude,
            l.longitude,
            l.category,
            l.is_default,
            l.nearest_road_name,
            l.nearest_road_highway,
            l.nearest_road_distance
        FROM locations l
        ORDER BY ST_SetSRID(ST_MakePoint(l.longitude, l.latitude), 4326)
               <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)
        LIMIT 1
    """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new LocationResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("category"),
                        rs.getBoolean("is_default"),
                        rs.getString("nearest_road_name"),
                        rs.getString("nearest_road_highway"),
                        rs.getDouble("nearest_road_distance")
                ),
                lon, lat
        );
    }

    public ReviewCheckResult findExistingReview(Long locationId, Long userId) {
        String sql = """
        SELECT rating, comment
        FROM reviews
        WHERE location_id = ? AND user_id = ?
    """;
        try {
            return jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new ReviewCheckResult(
                            rs.getDouble("rating"),
                            rs.getString("comment")
                    ),
                    locationId, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<LocationResponseDto> searchByName(String query, int limit) {
        String sql = """
        SELECT id, name, description, latitude, longitude, category,
               is_default, nearest_road_name, nearest_road_highway, nearest_road_distance
        FROM locations
        WHERE LOWER(name) LIKE LOWER(?)
        ORDER BY name
        LIMIT ?
    """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new LocationResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("category"),
                        rs.getBoolean("is_default"),
                        rs.getString("nearest_road_name"),
                        rs.getString("nearest_road_highway"),
                        rs.getDouble("nearest_road_distance")
                ),
                "%" + query + "%", limit
        );
    }
}
