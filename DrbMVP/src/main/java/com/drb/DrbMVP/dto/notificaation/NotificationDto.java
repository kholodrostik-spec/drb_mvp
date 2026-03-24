package com.drb.DrbMVP.dto.notificaation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Notification data")
public class NotificationDto {

    @Schema(example = "Welcome to DrbMVP")
    private String title;

    @Schema(example = "Thank you for registering!")
    private String body;
}
