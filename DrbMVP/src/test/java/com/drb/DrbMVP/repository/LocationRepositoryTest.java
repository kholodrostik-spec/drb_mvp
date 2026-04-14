package com.drb.DrbMVP.repository;

import com.drb.DrbMVP.dto.location.LocationDto;
import com.drb.DrbMVP.dto.location.LocationResponseDto;
import com.drb.DrbMVP.dto.location.NearestPointDto;
import com.drb.DrbMVP.dto.location.ReviewCheckResult;
import com.drb.DrbMVP.dto.review.ReviewDto;
import com.drb.DrbMVP.dto.review.ReviewResponseDto;
import com.drb.DrbMVP.service.S3Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class LocationRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private LocationRepository locationRepository;

    @Test
    void saveLocation_returnsDto() {
        LocationDto dto = new LocationDto("Test", "Desc", 53.3, -6.2, "PARK");
        NearestPointDto nearest = new NearestPointDto(1L, "Main St", "residential", 10.0, -6.2, 53.3);
        LocationResponseDto expected = new LocationResponseDto(1L, "Test", "Desc", 53.3, -6.2, "PARK", false, "Main St", "residential", 10.0);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<LocationResponseDto>>any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        )).thenReturn(expected);

        LocationResponseDto result = locationRepository.saveLocation(dto, nearest);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Test", result.getName());
    }

    @Test
    void saveReview_returnsDto() {
        ReviewDto dto = new ReviewDto();
        dto.setLocationId(1L);
        dto.setUserId(2L);
        dto.setRating(4.5);
        dto.setComment("Great");
        dto.setPhotoS3Key("reviews/1/photo.jpg");

        ReviewResponseDto expected = new ReviewResponseDto(1L, 2L, 4.5, "Great", LocalDateTime.now(), "https://s3/url");

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<ReviewResponseDto>>any(),
                Mockito.eq(1L), Mockito.eq(2L), Mockito.eq(4.5), Mockito.eq("Great"), Mockito.eq("reviews/1/photo.jpg")
        )).thenReturn(expected);

        ReviewResponseDto result = locationRepository.saveReview(dto, s3Service);

        Assertions.assertEquals(4.5, result.getRating(), 0.001);
        Assertions.assertEquals("Great", result.getComment());
    }

    @Test
    void existsUser_returnsTrueWhenCountPositive() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class), Mockito.eq(7L)))
                .thenReturn(1);

        boolean result = locationRepository.existsUser(7L);

        Assertions.assertTrue(result);
    }

    @Test
    void existsLocation_returnsFalseWhenCountZero() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class), Mockito.eq(9L)))
                .thenReturn(0);

        boolean result = locationRepository.existsLocation(9L);

        Assertions.assertFalse(result);
    }

    @Test
    void getPhotoS3Key_returnsKey() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(String.class), Mockito.eq(1L), Mockito.eq(2L)))
                .thenReturn("reviews/1/photo.jpg");

        String result = locationRepository.getPhotoS3Key(1L, 2L);

        Assertions.assertEquals("reviews/1/photo.jpg", result);
    }

    @Test
    void clearPhoto_callsUpdate() {
        locationRepository.clearPhoto(1L, 2L);
        Mockito.verify(jdbcTemplate).update(Mockito.anyString(), Mockito.eq(1L), Mockito.eq(2L));
    }

    @Test
    void clearComment_callsUpdate() {
        locationRepository.clearComment(1L, 2L);
        Mockito.verify(jdbcTemplate).update(Mockito.anyString(), Mockito.eq(1L), Mockito.eq(2L));
    }

    @Test
    void clearRating_callsUpdate() {
        locationRepository.clearRating(1L, 2L);
        Mockito.verify(jdbcTemplate).update(Mockito.anyString(), Mockito.eq(1L), Mockito.eq(2L));
    }

    @Test
    void findNearestLocation_returnsDto() {
        LocationResponseDto expected = new LocationResponseDto(1L, "Nearest", "Desc", 53.3, -6.2, "PARK", false, "Main St", "residential", 10.0);

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<LocationResponseDto>>any(),
                Mockito.eq(-6.2), Mockito.eq(53.3)
        )).thenReturn(expected);

        LocationResponseDto result = locationRepository.findNearestLocation(53.3, -6.2);

        Assertions.assertEquals("Nearest", result.getName());
    }

    @Test
    void findExistingReview_returnsReview() {
        ReviewCheckResult expected = new ReviewCheckResult(4.5, "Great", "https://s3/url");

        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<ReviewCheckResult>>any(),
                Mockito.eq(1L), Mockito.eq(2L)
        )).thenReturn(expected);

        ReviewCheckResult result = locationRepository.findExistingReview(1L, 2L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(4.5, result.getRating(), 0.001);
    }

    @Test
    void findExistingReview_emptyResult_returnsNull() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.<RowMapper<ReviewCheckResult>>any(),
                Mockito.eq(1L), Mockito.eq(2L)
        )).thenThrow(new EmptyResultDataAccessException(1));

        ReviewCheckResult result = locationRepository.findExistingReview(1L, 2L);

        Assertions.assertNull(result);
    }

    @Test
    void searchByName_returnsList() {
        List<LocationResponseDto> expected = List.of(
                new LocationResponseDto(1L, "Test Park", "Desc", 53.3, -6.2, "PARK", false, "Main St", "residential", 10.0)
        );

        Mockito.when(jdbcTemplate.query(
                Mockito.anyString(),
                Mockito.<RowMapper<LocationResponseDto>>any(),
                Mockito.eq("%Park%"),
                Mockito.eq("%Park%"),
                Mockito.eq(5)
        )).thenReturn(expected);

        List<LocationResponseDto> result = locationRepository.searchByName("Park", 5);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Test Park", result.get(0).getName());
    }
}