package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.location.LocationDto;
import com.drb.DrbMVP.dto.location.LocationResponseDto;
import com.drb.DrbMVP.dto.location.NearestPointDto;
import com.drb.DrbMVP.dto.review.ReviewDto;
import com.drb.DrbMVP.dto.review.ReviewResponseDto;
import com.drb.DrbMVP.repository.LocationRepository;
import com.drb.DrbMVP.repository.MapRepository;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final MapRepository mapRepository;

    public LocationService(LocationRepository locationRepository, MapRepository mapRepository) {
        this.locationRepository = locationRepository;
        this.mapRepository = mapRepository;
    }

    public LocationResponseDto addLocation(LocationDto dto) {
        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        NearestPointDto nearest = mapRepository.findNearestRoad(
                dto.getLatitude(), dto.getLongitude()
        );

        return locationRepository.saveLocation(dto, nearest);
    }

    public ReviewResponseDto addReview(ReviewDto dto) {
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (!locationRepository.existsUser(dto.getUserId())) {
            throw new IllegalArgumentException("User with id " + dto.getUserId() + " does not exist");
        }
        if (!locationRepository.existsLocation(dto.getLocationId())) {
            throw new IllegalArgumentException("Location with id " + dto.getLocationId() + " does not exist");
        }
        return locationRepository.saveReview(dto);
    }

    private void validateCoordinates(double lat, double lon) {
        if (lat < 51.3 || lat > 55.4 || lon < -10.7 || lon > -5.9) {
            throw new IllegalArgumentException("Coordinates must be within Ireland");
        }
    }
}
