package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.route.RoutePreferencesDto;
import com.drb.DrbMVP.dto.route.UserRoutePreferences;
import com.drb.DrbMVP.repository.RoutePreferencesRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RoutePreferencesServiceTest {

    @Mock
    private RoutePreferencesRepository repo;

    @InjectMocks
    private RoutePreferencesService service;

    private static final Long USER_ID = 1L;

    @Test
    void getPreferences_existingUser_returnsStoredPreferences() {
        UserRoutePreferences stored = new UserRoutePreferences(USER_ID, 0.3, 0.2, 0.1, 0.2, 0.1, 0.1);
        Mockito.when(repo.findByUserId(USER_ID)).thenReturn(Optional.of(stored));

        RoutePreferencesDto result = service.getPreferences(USER_ID);

        Assertions.assertEquals(0.3, result.getWTime(), 0.001);
        Assertions.assertEquals(0.2, result.getWSafety(), 0.001);
        Assertions.assertEquals(0.1, result.getWSimplicity(), 0.001);
        Assertions.assertEquals(0.2, result.getWBeauty(), 0.001);
    }

    @Test
    void getPreferences_noStoredPreferences_returnsDefaults() {
        Mockito.when(repo.findByUserId(USER_ID)).thenReturn(Optional.empty());
        UserRoutePreferences defaults = UserRoutePreferences.defaults(USER_ID);

        RoutePreferencesDto result = service.getPreferences(USER_ID);

        Assertions.assertEquals(defaults.getWTime(), result.getWTime(), 0.001);
        Assertions.assertEquals(defaults.getWSafety(), result.getWSafety(), 0.001);
        Assertions.assertEquals(defaults.getWSimplicity(), result.getWSimplicity(), 0.001);
        Assertions.assertEquals(defaults.getWBeauty(), result.getWBeauty(), 0.001);
    }

    @Test
    void updatePreferences_callsUpsertAndReturnsDto() {
        RoutePreferencesDto dto = new RoutePreferencesDto(0.5, 0.1, 0.1, 0.1, 0.1, 0.1);

        RoutePreferencesDto result = service.updatePreferences(USER_ID, dto);

        Mockito.verify(repo).upsert(Mockito.argThat(p ->
                p.getUserId().equals(USER_ID) &&
                        Math.abs(p.getWTime() - 0.5) < 0.001
        ));
        Assertions.assertEquals(0.5, result.getWTime(), 0.001);
    }

    @Test
    void resetToDefault_callsResetAndReturnsDefaults() {
        UserRoutePreferences defaults = UserRoutePreferences.defaults(USER_ID);

        RoutePreferencesDto result = service.resetToDefault(USER_ID);

        Mockito.verify(repo).resetToDefault(USER_ID);
        Assertions.assertEquals(defaults.getWTime(), result.getWTime(), 0.001);
        Assertions.assertEquals(defaults.getWSafety(), result.getWSafety(), 0.001);
    }
}
