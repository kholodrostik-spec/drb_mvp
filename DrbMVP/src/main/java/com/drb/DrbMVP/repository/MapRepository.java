package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.NearestPointDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MapRepository {

    private final JdbcTemplate jdbcTemplate;

    public MapRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public NearestPointDto findNearestRoad(double lat, double lon) {
        String sql = """
            WITH point AS (
                SELECT ST_SetSRID(ST_MakePoint(?, ?), 4326) AS geom
            )
            SELECT
                rn.rid,
                rn.name,
                rn.highway,
                ST_Distance(rn.geom::geography, p.geom::geography) AS distance,
                ST_X(ST_ClosestPoint(rn.geom, p.geom)) AS longitude,
                ST_Y(ST_ClosestPoint(rn.geom, p.geom)) AS latitude
            FROM road_network rn, point p
            ORDER BY rn.geom <-> p.geom
            LIMIT 1
        """;

        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new NearestPointDto(
                        rs.getLong("rid"),
                        rs.getString("name"),
                        rs.getString("highway"),
                        rs.getDouble("distance"),
                        rs.getDouble("longitude"),
                        rs.getDouble("latitude")
                ),
                lon, lat
        );
    }
}
