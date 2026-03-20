package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.NearestPointDto;
import com.drb.DrbMVP.dto.PlaceDto;
import com.drb.DrbMVP.dto.PlaceResponseDto;
import com.drb.DrbMVP.repository.MapRepository;
import com.drb.DrbMVP.repository.PlaceRepository;
import org.springframework.stereotype.Service;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final MapRepository mapRepository;

    public PlaceService(PlaceRepository placeRepository, MapRepository mapRepository) {
        this.placeRepository = placeRepository;
        this.mapRepository = mapRepository;
    }

    public PlaceResponseDto save(PlaceDto dto) {
        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        NearestPointDto nearest = mapRepository.findNearestRoad(
                dto.getLatitude(), dto.getLongitude()
        );

        return placeRepository.save(dto, nearest);
    }

    private void validateCoordinates(double lat, double lon) {
        if (lat < 51.3 || lat > 55.4 || lon < -10.7 || lon > -5.9) {
            throw new IllegalArgumentException("Coordinates must be within Ireland");
        }
    }
}
