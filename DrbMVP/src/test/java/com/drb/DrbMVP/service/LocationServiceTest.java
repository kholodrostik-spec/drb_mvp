package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.location.*;
import com.drb.DrbMVP.dto.review.ReviewDto;
import com.drb.DrbMVP.dto.review.ReviewResponseDto;
import com.drb.DrbMVP.repository.LocationRepository;
import com.drb.DrbMVP.repository.MapRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private MapRepository mapRepository;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private LocationService locationService;

    private static final double VALID_LAT = 53.3498;
    private static final double VALID_LON = -6.2603;

    private LocationDto locationDto;
    private NearestPointDto nearestPointDto;
    private LocationResponseDto locationResponseDto;

    @BeforeEach
    void setUp() {
        locationDto = new LocationDto();
        locationDto.setName("Test Park");
        locationDto.setDescription("A park");
        locationDto.setCategory("PARK");
        locationDto.setLatitude(VALID_LAT);
        locationDto.setLongitude(VALID_LON);

        nearestPointDto = new NearestPointDto(1L, "Main St", "residential", 10.0, VALID_LON, VALID_LAT);

        locationResponseDto = new LocationResponseDto(
                1L, "Test Park", "A park", VALID_LAT, VALID_LON,
                "PARK", false, "Main St", "residential", 10.0
        );
    }

    @Test
    void addLocation_validCoordinates_returnsLocationResponseDto() {
        Mockito.when(mapRepository.findNearestRoad(VALID_LAT, VALID_LON)).thenReturn(nearestPointDto);
        Mockito.when(locationRepository.saveLocation(locationDto, nearestPointDto)).thenReturn(locationResponseDto);

        LocationResponseDto result = locationService.addLocation(locationDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Test Park", result.getName());
        Assertions.assertEquals(VALID_LAT, result.getLatitude(), 0.0001);
    }

    @Test
    void addLocation_outsideIreland_throwsIllegalArgumentException() {
        locationDto.setLatitude(50.0);
        locationDto.setLongitude(30.0);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> locationService.addLocation(locationDto));

        Mockito.verify(mapRepository, Mockito.never()).findNearestRoad(Mockito.anyDouble(), Mockito.anyDouble());
    }

    @Test
    void addReview_validData_noPhoto_returnsReviewResponseDto() {
        ReviewDto dto = new ReviewDto();
        dto.setLocationId(1L);
        dto.setUserId(1L);
        dto.setRating(4.0);
        dto.setComment("Nice!");

        ReviewResponseDto expected = new ReviewResponseDto(
                1L, 1L, 4.0, "Nice!", LocalDateTime.now(), null
        );

        Mockito.when(locationRepository.existsUser(1L)).thenReturn(true);
        Mockito.when(locationRepository.existsLocation(1L)).thenReturn(true);
        Mockito.when(locationRepository.saveReview(dto, s3Service)).thenReturn(expected);

        ReviewResponseDto result = locationService.addReview(dto, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(4.0, result.getRating(), 0.01);
        Assertions.assertEquals("Nice!", result.getComment());
        Mockito.verify(s3Service, Mockito.never()).upload(Mockito.any(), Mockito.any());
    }

    @Test
    void addReview_withPhoto_uploadsToS3() {
        ReviewDto dto = new ReviewDto();
        dto.setLocationId(1L);
        dto.setUserId(1L);
        dto.setRating(3.0);
        dto.setComment("Ok");

        MockMultipartFile photo = new MockMultipartFile(
                "photo", "photo.jpg", "image/jpeg", new byte[100]
        );

        Mockito.when(locationRepository.existsUser(1L)).thenReturn(true);
        Mockito.when(locationRepository.existsLocation(1L)).thenReturn(true);
        Mockito.when(s3Service.upload(photo, "reviews/1")).thenReturn("reviews/1/uuid.jpg");
        Mockito.when(locationRepository.saveReview(dto, s3Service))
                .thenReturn(new ReviewResponseDto(1L, 1L, 3.0, "Ok", LocalDateTime.now(), "https://s3/url"));

        ReviewResponseDto result = locationService.addReview(dto, photo);

        Assertions.assertNotNull(result);
        Mockito.verify(s3Service).upload(photo, "reviews/1");
    }

    @Test
    void addReview_invalidRating_throwsIllegalArgumentException() {
        ReviewDto dto = new ReviewDto();
        dto.setRating(6.0);
        dto.setUserId(1L);
        dto.setLocationId(1L);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> locationService.addReview(dto, null));
    }

    @Test
    void addReview_userNotFound_throwsIllegalArgumentException() {
        ReviewDto dto = new ReviewDto();
        dto.setRating(4.0);
        dto.setUserId(99L);
        dto.setLocationId(1L);

        Mockito.when(locationRepository.existsUser(99L)).thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> locationService.addReview(dto, null));
    }

    @Test
    void addReview_locationNotFound_throwsIllegalArgumentException() {
        ReviewDto dto = new ReviewDto();
        dto.setRating(4.0);
        dto.setUserId(1L);
        dto.setLocationId(99L);

        Mockito.when(locationRepository.existsUser(1L)).thenReturn(true);
        Mockito.when(locationRepository.existsLocation(99L)).thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> locationService.addReview(dto, null));
    }

    @Test
    void deleteReviewPhoto_withS3Key_deletesFromS3AndClears() {
        Mockito.when(locationRepository.getPhotoS3Key(1L, 1L)).thenReturn("reviews/1/photo.jpg");

        locationService.deleteReviewPhoto(1L, 1L);

        Mockito.verify(s3Service).delete("reviews/1/photo.jpg");
        Mockito.verify(locationRepository).clearPhoto(1L, 1L);
    }

    @Test
    void deleteReviewPhoto_noS3Key_doesNotCallDelete() {
        Mockito.when(locationRepository.getPhotoS3Key(1L, 1L)).thenReturn(null);

        locationService.deleteReviewPhoto(1L, 1L);

        Mockito.verify(s3Service, Mockito.never()).delete(Mockito.any());
        Mockito.verify(locationRepository, Mockito.never()).clearPhoto(Mockito.any(), Mockito.any());
    }

    @Test
    void deleteReviewComment_callsClearComment() {
        locationService.deleteReviewComment(1L, 1L);
        Mockito.verify(locationRepository).clearComment(1L, 1L);
    }

    @Test
    void deleteReviewRating_callsClearRating() {
        locationService.deleteReviewRating(1L, 1L);
        Mockito.verify(locationRepository).clearRating(1L, 1L);
    }

    @Test
    void findNearestLocation_delegatesToRepository() {
        Mockito.when(locationRepository.findNearestLocation(VALID_LAT, VALID_LON)).thenReturn(locationResponseDto);

        LocationResponseDto result = locationService.findNearestLocation(VALID_LAT, VALID_LON);

        Assertions.assertEquals(locationResponseDto.getId(), result.getId());
    }

    @Test
    void searchByName_returnsListFromRepository() {
        Mockito.when(locationRepository.searchByName("Park", 5)).thenReturn(List.of(locationResponseDto));

        List<LocationResponseDto> result = locationService.searchByName("Park", 5);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Test Park", result.get(0).getName());
    }

    @Test
    void checkExistingReview_locationFound_reviewExists_returnsReviewCheckDto() {
        Mockito.when(locationRepository.findNearestLocation(VALID_LAT, VALID_LON)).thenReturn(locationResponseDto);
        Mockito.when(locationRepository.findExistingReview(1L, 1L))
                .thenReturn(new ReviewCheckResult(4.5, "Great", "https://s3/photo"));

        ReviewCheckDto result = locationService.checkExistingReview(VALID_LAT, VALID_LON, 1L);

        Assertions.assertTrue(result.isHasReview());
        Assertions.assertEquals(1L, result.getLocationId());
        Assertions.assertEquals(4.5, result.getExistingRating(), 0.01);
        Assertions.assertEquals("Great", result.getExistingComment());
    }

    @Test
    void checkExistingReview_locationFound_noReview_returnsNoReview() {
        Mockito.when(locationRepository.findNearestLocation(VALID_LAT, VALID_LON)).thenReturn(locationResponseDto);
        Mockito.when(locationRepository.findExistingReview(1L, 1L)).thenReturn(null);

        ReviewCheckDto result = locationService.checkExistingReview(VALID_LAT, VALID_LON, 1L);

        Assertions.assertFalse(result.isHasReview());
        Assertions.assertEquals(1L, result.getLocationId());
        Assertions.assertNull(result.getExistingRating());
    }

    @Test
    void checkExistingReview_noLocationFound_returnsFalse() {
        Mockito.when(locationRepository.findNearestLocation(VALID_LAT, VALID_LON)).thenReturn(null);

        ReviewCheckDto result = locationService.checkExistingReview(VALID_LAT, VALID_LON, 1L);

        Assertions.assertFalse(result.isHasReview());
        Assertions.assertNull(result.getLocationId());
    }
}
