package com.drb.DrbMVP.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoutePreferences {
    private Long userId;
    private double wTime;
    private double wSafety;
    private double wSimplicity;
    private double wBeauty;
    private double wResidential;
    private double wMinor;

    public static UserRoutePreferences defaults(Long userId) {
        return new UserRoutePreferences(userId, 0.4, 0.2, 0.15, 0.15, 0.05, 0.05);
    }
}