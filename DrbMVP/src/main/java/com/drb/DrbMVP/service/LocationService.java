package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.location.*;
import com.drb.DrbMVP.dto.review.ReviewDto;
import com.drb.DrbMVP.dto.review.ReviewResponseDto;
import com.drb.DrbMVP.repository.LocationRepository;
import com.drb.DrbMVP.repository.MapRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final MapRepository mapRepository;
    private final S3Service s3Service;

    public LocationService(LocationRepository locationRepository,
                           MapRepository mapRepository,
                           S3Service s3Service) {
        this.locationRepository = locationRepository;
        this.mapRepository = mapRepository;
        this.s3Service = s3Service;
    }
    public LocationResponseDto addLocation(LocationDto dto) {
        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        NearestPointDto nearest = mapRepository.findNearestRoad(
                dto.getLatitude(), dto.getLongitude()
        );

        return locationRepository.saveLocation(dto, nearest);
    }

    public ReviewResponseDto addReview(ReviewDto dto, MultipartFile photo) {
        log.info("Service received photo: {}", photo != null ? photo.getOriginalFilename() + " size=" + photo.getSize() : "NULL");
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (!locationRepository.existsUser(dto.getUserId())) {
            throw new IllegalArgumentException("User with id " + dto.getUserId() + " does not exist");
        }
        if (!locationRepository.existsLocation(dto.getLocationId())) {
            throw new IllegalArgumentException("Location with id " + dto.getLocationId() + " does not exist");
        }
        if (photo != null && !photo.isEmpty()) {
            String s3Key = s3Service.upload(photo, "reviews/" + dto.getLocationId());
            dto.setPhotoS3Key(s3Key);
        }

        return locationRepository.saveReview(dto, s3Service);
    }

    private void validateCoordinates(double lat, double lon) {
        if (lat < 51.3 || lat > 55.4 || lon < -10.7 || lon > -5.9) {
            throw new IllegalArgumentException("Coordinates must be within Ireland");
        }
    }

    public void deleteReviewPhoto(Long locationId, Long userId) {
        String s3Key = locationRepository.getPhotoS3Key(locationId, userId);
        if (s3Key != null) {
            s3Service.delete(s3Key);
            locationRepository.clearPhoto(locationId, userId);
        }
    }

    public void deleteReviewComment(Long locationId, Long userId) {
        locationRepository.clearComment(locationId, userId);
    }

    public void deleteReviewRating(Long locationId, Long userId) {
        locationRepository.clearRating(locationId, userId);
    }

    public LocationResponseDto findNearestLocation(double lat, double lon) {
        return locationRepository.findNearestLocation(lat, lon);
    }

    public ReviewCheckDto checkExistingReview(double lat, double lon, Long userId) {
        LocationResponseDto nearest = findNearestLocation(lat, lon);
        if (nearest == null) {
            return new ReviewCheckDto(false, null, null, null, null);
        }

        ReviewCheckResult existing = locationRepository.findExistingReview(nearest.getId(), userId);

        if (existing != null) {
            return new ReviewCheckDto(
                    true,
                    nearest.getId(),
                    nearest.getName(),
                    existing.getRating(),
                    existing.getComment()
            );
        }

        return new ReviewCheckDto(false, nearest.getId(), nearest.getName(), null, null);
    }

    public List<LocationResponseDto> searchByName(String query, int limit) {
        return locationRepository.searchByName(query, limit);
    }
}
