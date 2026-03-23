package com.drb.DrbMVP.service.service_api;

import com.drb.DrbMVP.dto.notificaation.NotificationDto;

public interface EmailService {
    void sendNotificationByEmail(NotificationDto notification, String email);
    void sendRegistrationEmail(String email, String name);
}
