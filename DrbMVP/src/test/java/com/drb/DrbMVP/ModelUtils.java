package com.drb.DrbMVP;

import com.drb.DrbMVP.dto.apilog.ApiLogDto;
import com.drb.DrbMVP.dto.location.LocationDto;
import com.drb.DrbMVP.dto.location.NearestPointDto;
import com.drb.DrbMVP.dto.route.UserRoutePreferences;
import com.drb.DrbMVP.entity.Street;

public class ModelUtils {

    public static ApiLogDto getApiLogDto() {
        return new ApiLogDto(
                1L,
                "test@example.com",
                "GET",
                "/api/test",
                "param=value",
                "{\"key\":\"value\"}",
                200,
                42L
        );
    }

    public static String getTestEmail() {
        return "test@example.com";
    }

    public static LocationDto getLocationDto() {
        LocationDto dto = new LocationDto();
        dto.setName("Test Location");
        dto.setDescription("Test description");
        dto.setCategory("PARK");
        dto.setLatitude(50.4501);
        dto.setLongitude(30.5234);
        return dto;
    }

    public static NearestPointDto getNearestPointDto() {
        return new NearestPointDto(
                1L,
                "Test Street",
                "residential",
                15.0,
                30.5235,
                50.4502
        );
    }

    public static UserRoutePreferences getUserRoutePreferences(Long userId) {
        return new UserRoutePreferences(
                userId,
                0.3,
                0.2,
                0.1,
                0.2,
                0.1,
                0.1
        );
    }

    public static UserRoutePreferences getDefaultRoutePreferences(Long userId) {
        return UserRoutePreferences.defaults(userId);
    }

    public static Street getStreet() {
        Street street = new Street();
        street.setName("Main Street");
        street.setHighway("residential");
        return street;
    }

    public static String getUserEmail() {
        return "user@example.com";
    }

    public static String getUserName() {
        return "Test User";
    }

    public static String getUserHashedPassword() {
        return "$2a$10$hashedpasswordexample";
    }
}
