package com.drb.DrbMVP.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private Long userId;
    private String email;
    private String role;
}
