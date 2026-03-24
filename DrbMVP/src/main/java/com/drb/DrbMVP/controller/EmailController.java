package com.drb.DrbMVP.controller;

import com.drb.DrbMVP.dto.notificaation.NotificationDto;
import com.drb.DrbMVP.service.service_api.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@Tag(name = "Email API", description = "Endpoints for sending emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @Operation(summary = "Send notification to user via email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/notification")
    public ResponseEntity<Object> sendUserNotification(
            @RequestBody NotificationDto notification,
            @RequestParam String email) {
        emailService.sendNotificationByEmail(notification, email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
