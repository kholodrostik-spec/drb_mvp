package com.drb.DrbMVP.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class TransportRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findNearestStops(double lat, double lon) {
        String sql = """
            SELECT
                stop_id,
                stop_name,
                latitude,
                longitude,
                ST_Distance(
                    geom::geography,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                ) AS distance_m
            FROM transport_stops
            ORDER BY geom <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)
            LIMIT 3
        """;
        return jdbcTemplate.queryForList(sql, lon, lat, lon, lat);
    }

    public Map<String, Object> findStopDetails(String stopId) {
        String stopSql = """
            SELECT stop_id, stop_name, latitude, longitude
            FROM transport_stops
            WHERE stop_id = ?
        """;
        return jdbcTemplate.queryForMap(stopSql, stopId);
    }

    public List<Map<String, Object>> findNextDepartures(String stopId) {
        String sql = """
            SELECT
                st.arrival_time,
                st.departure_time,
                st.stop_sequence,
                t.trip_headsign,
                r.route_short,
                r.route_long,
                r.route_type
            FROM transport_stop_times st
            JOIN transport_trips t ON st.trip_id = t.trip_id
            JOIN transport_routes r ON t.route_id = r.route_id
            WHERE st.stop_id = ?
              AND st.departure_time >= (
                  EXTRACT(HOUR FROM NOW()) * 3600 +
                  EXTRACT(MINUTE FROM NOW()) * 60 +
                  EXTRACT(SECOND FROM NOW())
              ) * INTERVAL '1 second'
            ORDER BY st.departure_time
            LIMIT 5
        """;
        return jdbcTemplate.queryForList(sql, stopId);
    }

    public String findRouteGeoJson(String routeShort) {
        String sql = """
        SELECT ST_AsGeoJSON(
            ST_MakeLine(
                ts.geom ORDER BY st.stop_sequence
            )
        ) AS route_geojson
        FROM transport_stops ts
        JOIN transport_stop_times st ON ts.stop_id = st.stop_id
        JOIN transport_trips t ON st.trip_id = t.trip_id
        JOIN transport_routes r ON t.route_id = r.route_id
        WHERE r.route_short = ?
        AND t.trip_id = (
            SELECT trip_id FROM transport_trips t2
            JOIN transport_routes r2 ON t2.route_id = r2.route_id
            WHERE r2.route_short = ?
            LIMIT 1
        )
    """;
        return jdbcTemplate.queryForObject(sql, String.class, routeShort, routeShort);
    }

    public Map<String, Object> findNearestStopOfRoute(String routeShort, double lat, double lon) {
        String sql = """
        SELECT
            ts.stop_id,
            ts.stop_name,
            ts.latitude,
            ts.longitude,
            ST_Distance(
                ts.geom::geography,
                ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
            ) AS distance_m
        FROM transport_stops ts
        JOIN transport_stop_times st ON ts.stop_id = st.stop_id
        JOIN transport_trips t ON st.trip_id = t.trip_id
        JOIN transport_routes r ON t.route_id = r.route_id
        WHERE r.route_short = ?
        ORDER BY ts.geom <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)
        LIMIT 1
    """;
        return jdbcTemplate.queryForMap(sql, lon, lat, routeShort, lon, lat);
    }
}
