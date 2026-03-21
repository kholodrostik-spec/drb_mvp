package com.drb.DrbMVP.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginDto {
    @Schema(example = "test@test.com")
    private String email;
    @Schema(example = "password123")
    private String password;
}