package com.drb.DrbMVP.dto.notificaation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Notification data")
public class NotificationDto {

    @Schema(example = "Welcome to DrbMVP")
    private String title;

    @Schema(example = "Thank you for registering!")
    private String body;
}
