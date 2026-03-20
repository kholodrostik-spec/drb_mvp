package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.NearestPointDto;
import com.drb.DrbMVP.dto.PlaceDto;
import com.drb.DrbMVP.dto.PlaceResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class PlaceRepository {

    private final JdbcTemplate jdbcTemplate;

    public PlaceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PlaceResponseDto save(PlaceDto dto, NearestPointDto nearest) {
        String sql = """
            INSERT INTO places (
                name, description,
                latitude, longitude,
                geom,
                nearest_road_name, nearest_road_highway, nearest_road_distance,
                rating, category
            ) VALUES (
                ?, ?,
                ?, ?,
                ST_SetSRID(ST_MakePoint(?, ?), 4326),
                ?, ?, ?,
                ?, ?
            )
            RETURNING id, name, description, latitude, longitude,
                      category, rating,
                      nearest_road_name, nearest_road_highway, nearest_road_distance
        """;

        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new PlaceResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("category"),
                        rs.getDouble("rating"),
                        rs.getString("nearest_road_name"),
                        rs.getString("nearest_road_highway"),
                        rs.getDouble("nearest_road_distance")
                ),
                dto.getName(), dto.getDescription(),
                dto.getLatitude(), dto.getLongitude(),
                dto.getLongitude(), dto.getLatitude(),
                nearest.getName(), nearest.getHighway(), nearest.getDistance(),
                dto.getRating(), dto.getCategory()
        );
    }
}
