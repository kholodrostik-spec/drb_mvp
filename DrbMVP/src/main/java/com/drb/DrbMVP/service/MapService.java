package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.NearestPointDto;
import com.drb.DrbMVP.repository.MapRepository;
import org.springframework.stereotype.Service;

@Service
public class MapService {

    private final MapRepository mapRepository;

    private static final double[][] IRELAND_POLYGON = {
            {55.4, -8.2}, {55.3, -6.2}, {54.0, -5.9}, {52.8, -6.0},
            {51.4, -8.5}, {51.3, -9.8}, {52.5, -10.7}, {53.8, -10.5},
            {54.5, -10.0}, {55.2, -7.6}
    };

    public MapService(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public NearestPointDto findNearestRoad(double lat, double lon) {
        validateCoordinates(lat, lon);
        return mapRepository.findNearestRoad(lat, lon);
    }

    private void validateCoordinates(double lat, double lon) {
        int n = IRELAND_POLYGON.length;
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = IRELAND_POLYGON[i][0], yi = IRELAND_POLYGON[i][1];
            double xj = IRELAND_POLYGON[j][0], yj = IRELAND_POLYGON[j][1];
            if (((yi > lon) != (yj > lon)) &&
                    (lat < (xj - xi) * (lon - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        if (!inside) {
            throw new IllegalArgumentException("Coordinates must be within Ireland");
        }
    }
}
