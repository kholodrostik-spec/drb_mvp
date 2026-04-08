package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.location.NearestPointDto;
import com.drb.DrbMVP.dto.route.RouteDto;
import com.drb.DrbMVP.exception.RouteNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
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

    public RouteDto findShortestRoute(
            double latFrom, double lonFrom,
            double latTo, double lonTo) {

        try {
            String nearestNodeSql = """
            WITH main_component_nodes AS (
               SELECT node
               FROM road_network_components
               WHERE component = 11
           ),
           closest_edge AS (
               SELECT rn.source, rn.target, rn.geom
               FROM road_network rn
               JOIN main_component_nodes ms
                 ON rn.source = ms.node OR rn.target = ms.node
               WHERE rn.highway IN (
                   'primary', 'primary_link',
                   'secondary', 'secondary_link',
                   'tertiary', 'tertiary_link',
                   'unclassified', 'residential',
                   'service'
               )
               ORDER BY rn.geom <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)
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
                       (SELECT geom FROM routable_vertices WHERE id = be.source),
                       ST_SetSRID(ST_MakePoint(?, ?), 4326)
                   ) < ST_Distance(
                       (SELECT geom FROM routable_vertices WHERE id = be.target),
                       ST_SetSRID(ST_MakePoint(?, ?), 4326)
                   )
                   THEN be.source
                   ELSE be.target
               END AS id
           FROM best_edge be
        """;

            Long sourceNode = jdbcTemplate.queryForObject(
                    nearestNodeSql, Long.class, lonFrom, latFrom, lonFrom, latFrom, lonFrom, latFrom, lonFrom, latFrom);
            Long targetNode = jdbcTemplate.queryForObject(
                    nearestNodeSql, Long.class, lonTo, latTo, lonTo, latTo, lonTo, latTo, lonTo, latTo);

            log.info("Source node: {}, Target node: {}", sourceNode, targetNode);

            String routeSql = """
                WITH route AS (
                    SELECT
                        SUM(r.cost) AS road_cost,
                        ST_MakeLine(
                            CASE WHEN r.node = rn.source\s
                                 THEN rn.geom\s
                                 ELSE ST_Reverse(rn.geom)\s
                            END
                            ORDER BY r.seq
                        ) AS route_geom
                    FROM pgr_dijkstra(
                        'SELECT rid AS id, source, target, cost, reverse_cost
                         FROM road_network
                         WHERE highway NOT IN (
                             ''footway'', ''cycleway'', ''path'', ''steps'',
                             ''pedestrian'', ''track'', ''bridleway'', ''corridor'',
                             ''platform'', ''construction'', ''proposed'', ''raceway'',
                             ''escape'', ''ladder'', ''bus_stop''
                         )',
                        ?, ?, true
                    ) r
                    JOIN road_network rn ON r.edge = rn.rid
                    WHERE r.edge != -1
                ),
                snap_start AS (
                    SELECT
                        ST_Distance(
                            ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                            v.geom::geography
                        ) AS dist,
                        ST_MakeLine(
                            ST_SetSRID(ST_MakePoint(?, ?), 4326),
                            v.geom
                        ) AS geom
                    FROM routable_vertices v WHERE v.id = ?
                ),
                snap_end AS (
                    SELECT
                        ST_Distance(
                            ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                            v.geom::geography
                        ) AS dist,
                        ST_MakeLine(
                            v.geom,
                            ST_SetSRID(ST_MakePoint(?, ?), 4326)
                        ) AS geom
                    FROM routable_vertices v WHERE v.id = ?
                )
                SELECT
                    route.road_cost + snap_start.dist + snap_end.dist AS total_cost,
                    route.road_cost,
                    snap_start.dist AS snap_start_cost,
                    snap_end.dist   AS snap_end_cost,
                    ST_AsGeoJSON(route.route_geom)    AS route_geojson,
                    ST_AsGeoJSON(snap_start.geom)     AS snap_start_geojson,
                    ST_AsGeoJSON(snap_end.geom)       AS snap_end_geojson
                FROM route, snap_start, snap_end
        """;

            return jdbcTemplate.queryForObject(routeSql,
                    (rs, rowNum) -> new RouteDto(
                            rs.getDouble("total_cost"),
                            rs.getDouble("road_cost"),
                            rs.getDouble("snap_start_cost"),
                            rs.getDouble("snap_end_cost"),
                            rs.getString("route_geojson"),
                            rs.getString("snap_start_geojson"),
                            rs.getString("snap_end_geojson")
                    ),
                    sourceNode, targetNode,
                    lonFrom, latFrom, lonFrom, latFrom, sourceNode,
                    lonTo, latTo, lonTo, latTo, targetNode
            );
        }catch (EmptyResultDataAccessException e) {
            throw new RouteNotFoundException(
                    "No route found between these points"
            );
        }

    }
}
