package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.route.RouteCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class CarRoutingRepository {

    private final JdbcTemplate jdbc;

    public CarRoutingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Find nearest routable car node for given coordinates
    public Long findNearestCarNode(double lat, double lon) {
        String sql = """
            WITH closest_edge AS (
                SELECT source, target, geom
                FROM car_main_component_edges
                ORDER BY geom <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)
                LIMIT 20
            ),
            best_edge AS (
                SELECT source, target, geom
                FROM closest_edge
                ORDER BY ST_Distance(geom, ST_SetSRID(ST_MakePoint(?, ?), 4326))
                LIMIT 1
            )
            SELECT
                CASE
                    WHEN ST_Distance(
                        (SELECT geom FROM car_routable_vertices WHERE id = be.source),
                        ST_SetSRID(ST_MakePoint(?, ?), 4326)
                    ) < ST_Distance(
                        (SELECT geom FROM car_routable_vertices WHERE id = be.target),
                        ST_SetSRID(ST_MakePoint(?, ?), 4326)
                    )
                    THEN be.source
                    ELSE be.target
                END AS id
            FROM best_edge be
        """;
        try {
            return jdbc.queryForObject(sql, Long.class,
                    lon, lat, lon, lat, lon, lat, lon, lat);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Build a single candidate route using a specific cost profile
    public RouteCandidate buildCandidate(
            String profile,
            Long sourceNode,
            Long targetNode,
            double latFrom, double lonFrom,
            double latTo, double lonTo,
            String costExpression,
            String reverseCostExpression) {

        String sql = String.format("""
            WITH route AS (
                SELECT
                    r.seq,
                    r.node,
                    r.edge,
                    r.cost,
                    rn.highway,
                    ST_Length(rn.geom::geography) AS seg_length_m,
                    CASE WHEN r.node = rn.source THEN rn.geom ELSE ST_Reverse(rn.geom) END AS geom
                FROM pgr_dijkstra(
                    'SELECT rid AS id, source, target,
                            %s AS cost,
                            %s AS reverse_cost
                     FROM car_main_component_edges',
                    %%s, %%s, true
                ) r
                JOIN car_main_component_edges rn ON r.edge = rn.rid
                WHERE r.edge <> -1
                ORDER BY r.seq
            ),
            metrics AS (
                SELECT
                    SUM(seg_length_m) AS total_m,
                    COUNT(*) AS segment_count,
                    SUM(CASE WHEN highway IN ('residential','living_street') THEN seg_length_m ELSE 0 END) AS residential_m,
                    SUM(CASE WHEN highway IN ('unclassified','track','service') THEN seg_length_m ELSE 0 END) AS minor_m,
                    ST_MakeLine(geom ORDER BY seq) AS route_geom
                FROM route
            ),
            turns AS (
                SELECT COUNT(*) AS turn_count
                FROM (
                    SELECT
                        seq,
                        ST_Azimuth(
                            ST_PointN(geom, 1), ST_PointN(geom, -1)
                        ) AS az
                    FROM route
                ) az_data
                WHERE ABS(az - LAG(az) OVER (ORDER BY seq)) > 0.2
            ),
            snap_start AS (
                SELECT ST_MakeLine(ST_SetSRID(ST_MakePoint(%%s, %%s), 4326), v.geom) AS geom
                FROM car_routable_vertices v WHERE v.id = %%s
            ),
            snap_end AS (
                SELECT ST_MakeLine(v.geom, ST_SetSRID(ST_MakePoint(%%s, %%s), 4326)) AS geom
                FROM car_routable_vertices v WHERE v.id = %%s
            )
            SELECT
                m.total_m,
                m.segment_count,
                m.residential_m,
                m.minor_m,
                t.turn_count,
                ST_AsGeoJSON(m.route_geom) AS route_geojson,
                ST_AsGeoJSON(ss.geom) AS snap_start_geojson,
                ST_AsGeoJSON(se.geom) AS snap_end_geojson
            FROM metrics m, turns t, snap_start ss, snap_end se
        """, costExpression, reverseCostExpression);

        try {
            return jdbc.queryForObject(sql,
                    (rs, rowNum) -> {
                        double totalM = rs.getDouble("total_m");
                        double residentialM = rs.getDouble("residential_m");
                        double minorM = rs.getDouble("minor_m");
                        int turnCount = rs.getInt("turn_count");
                        int segmentCount = rs.getInt("segment_count");

                        double residentialRatio = totalM > 0 ? residentialM / totalM : 0;
                        double minorRatio = totalM > 0 ? minorM / totalM : 0;

                        // Safety score: higher on residential/minor roads, lower on primary/motorway
                        double safetyScore = Math.min(1.0, residentialRatio * 0.6 + minorRatio * 0.4 + 0.2);

                        // Beauty score: higher on minor/unclassified roads (countryside feel)
                        double beautyScore = Math.min(1.0, minorRatio * 0.7 + residentialRatio * 0.2 + 0.1);

                        // Simplicity score: fewer turns = simpler
                        double simplicityScore = segmentCount > 0
                                ? Math.max(0, 1.0 - (double) turnCount / segmentCount)
                                : 0.5;

                        // Time estimate at 40 km/h average
                        double timeMin = (totalM / 1000.0) / 40.0 * 60.0;

                        return new RouteCandidate(
                                profile,
                                totalM,
                                timeMin,
                                turnCount,
                                residentialRatio,
                                minorRatio,
                                safetyScore,
                                beautyScore,
                                simplicityScore,
                                0.0, // totalScore computed later
                                rs.getString("route_geojson"),
                                rs.getString("snap_start_geojson"),
                                rs.getString("snap_end_geojson")
                        );
                    },
                    sourceNode, targetNode,
                    lonFrom, latFrom, sourceNode,
                    lonTo, latTo, targetNode
            );
        } catch (EmptyResultDataAccessException e) {
            log.warn("No route found for profile: {}", profile);
            return null;
        }
    }

    // Build all 5 candidate profiles
    public List<RouteCandidate> buildAllCandidates(
            Long sourceNode, Long targetNode,
            double latFrom, double lonFrom,
            double latTo, double lonTo) {

        List<RouteCandidate> candidates = new ArrayList<>();

        // fastest — minimize raw distance/cost
        addCandidate(candidates, "fastest", sourceNode, targetNode,
                latFrom, lonFrom, latTo, lonTo,
                "cost", "reverse_cost");

        // safest — heavily prefer residential and minor roads
        addCandidate(candidates, "safest", sourceNode, targetNode,
                latFrom, lonFrom, latTo, lonTo,
                """
                CASE
                    WHEN highway IN ('residential','living_street') THEN cost * 0.5
                    WHEN highway IN ('unclassified','service','track') THEN cost * 0.7
                    WHEN highway IN ('tertiary','tertiary_link') THEN cost * 1.2
                    WHEN highway IN ('secondary','secondary_link') THEN cost * 2.0
                    WHEN highway IN ('primary','primary_link') THEN cost * 3.5
                    WHEN highway IN ('trunk','trunk_link','motorway','motorway_link') THEN cost * 6.0
                    ELSE cost * 1.5
                END
                """,
                """
                CASE
                    WHEN highway IN ('residential','living_street') THEN reverse_cost * 0.5
                    WHEN highway IN ('unclassified','service','track') THEN reverse_cost * 0.7
                    WHEN highway IN ('tertiary','tertiary_link') THEN reverse_cost * 1.2
                    WHEN highway IN ('secondary','secondary_link') THEN reverse_cost * 2.0
                    WHEN highway IN ('primary','primary_link') THEN reverse_cost * 3.5
                    WHEN highway IN ('trunk','trunk_link','motorway','motorway_link') THEN reverse_cost * 6.0
                    ELSE reverse_cost * 1.5
                END
                """);

        // simplest — prefer larger roads with fewer junctions
        addCandidate(candidates, "simplest", sourceNode, targetNode,
                latFrom, lonFrom, latTo, lonTo,
                """
                CASE
                    WHEN highway IN ('motorway','motorway_link','trunk','trunk_link') THEN cost * 0.6
                    WHEN highway IN ('primary','primary_link') THEN cost * 0.8
                    WHEN highway IN ('secondary','secondary_link') THEN cost * 1.0
                    WHEN highway IN ('tertiary','tertiary_link') THEN cost * 1.3
                    WHEN highway IN ('residential','living_street') THEN cost * 2.0
                    WHEN highway IN ('unclassified','service','track') THEN cost * 2.5
                    ELSE cost
                END
                """,
                """
                CASE
                    WHEN highway IN ('motorway','motorway_link','trunk','trunk_link') THEN reverse_cost * 0.6
                    WHEN highway IN ('primary','primary_link') THEN reverse_cost * 0.8
                    WHEN highway IN ('secondary','secondary_link') THEN reverse_cost * 1.0
                    WHEN highway IN ('tertiary','tertiary_link') THEN reverse_cost * 1.3
                    WHEN highway IN ('residential','living_street') THEN reverse_cost * 2.0
                    WHEN highway IN ('unclassified','service','track') THEN reverse_cost * 2.5
                    ELSE reverse_cost
                END
                """);

        // scenic — prefer minor and unclassified roads
        addCandidate(candidates, "scenic", sourceNode, targetNode,
                latFrom, lonFrom, latTo, lonTo,
                """
                CASE
                    WHEN highway IN ('unclassified','track') THEN cost * 0.5
                    WHEN highway IN ('residential','living_street') THEN cost * 0.7
                    WHEN highway IN ('service') THEN cost * 0.9
                    WHEN highway IN ('tertiary','tertiary_link') THEN cost * 1.2
                    WHEN highway IN ('secondary','secondary_link') THEN cost * 2.0
                    WHEN highway IN ('primary','primary_link') THEN cost * 3.0
                    WHEN highway IN ('trunk','trunk_link','motorway','motorway_link') THEN cost * 5.0
                    ELSE cost
                END
                """,
                """
                CASE
                    WHEN highway IN ('unclassified','track') THEN reverse_cost * 0.5
                    WHEN highway IN ('residential','living_street') THEN reverse_cost * 0.7
                    WHEN highway IN ('service') THEN reverse_cost * 0.9
                    WHEN highway IN ('tertiary','tertiary_link') THEN reverse_cost * 1.2
                    WHEN highway IN ('secondary','secondary_link') THEN reverse_cost * 2.0
                    WHEN highway IN ('primary','primary_link') THEN reverse_cost * 3.0
                    WHEN highway IN ('trunk','trunk_link','motorway','motorway_link') THEN reverse_cost * 5.0
                    ELSE reverse_cost
                END
                """);

        // balanced — moderate mix
        addCandidate(candidates, "balanced", sourceNode, targetNode,
                latFrom, lonFrom, latTo, lonTo,
                """
                CASE
                    WHEN highway IN ('residential','living_street') THEN cost * 0.8
                    WHEN highway IN ('unclassified','service') THEN cost * 0.9
                    WHEN highway IN ('tertiary','tertiary_link') THEN cost * 1.0
                    WHEN highway IN ('secondary','secondary_link') THEN cost * 1.1
                    WHEN highway IN ('primary','primary_link') THEN cost * 1.5
                    WHEN highway IN ('trunk','trunk_link','motorway','motorway_link') THEN cost * 2.0
                    ELSE cost
                END
                """,
                """
                CASE
                    WHEN highway IN ('residential','living_street') THEN reverse_cost * 0.8
                    WHEN highway IN ('unclassified','service') THEN reverse_cost * 0.9
                    WHEN highway IN ('tertiary','tertiary_link') THEN reverse_cost * 1.0
                    WHEN highway IN ('secondary','secondary_link') THEN reverse_cost * 1.1
                    WHEN highway IN ('primary','primary_link') THEN reverse_cost * 1.5
                    WHEN highway IN ('trunk','trunk_link','motorway','motorway_link') THEN reverse_cost * 2.0
                    ELSE reverse_cost
                END
                """);

        return candidates;
    }

    private void addCandidate(List<RouteCandidate> list, String profile,
                              Long src, Long tgt,
                              double latFrom, double lonFrom,
                              double latTo, double lonTo,
                              String cost, String reverseCost) {
        RouteCandidate c = buildCandidate(profile, src, tgt,
                latFrom, lonFrom, latTo, lonTo, cost, reverseCost);
        if (c != null) list.add(c);
    }
}
