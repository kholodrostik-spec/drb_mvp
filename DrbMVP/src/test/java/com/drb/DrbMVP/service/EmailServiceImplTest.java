package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.notificaation.NotificationDto;
import com.drb.DrbMVP.repository.UserRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private static final String EMAIL = "test@example.com";
    private static final String NAME  = "Test User";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmailAddress", "noreply@test.com");
    }

    @Test
    void sendNotificationByEmail_existingUser_sendsMail() {
        NotificationDto notification = new NotificationDto("Subject", "Body");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));

        Mockito.when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(Map.of("email", EMAIL)));
        Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendNotificationByEmail(notification, EMAIL);

        Mockito.verify(javaMailSender).createMimeMessage();
        Mockito.verify(javaMailSender, Mockito.timeout(1000)).send(Mockito.any(MimeMessage.class));
    }

    @Test
    void sendNotificationByEmail_userNotFound_throwsIllegalArgumentException() {
        NotificationDto notification = new NotificationDto("Subject", "Body");
        Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> emailService.sendNotificationByEmail(notification, EMAIL));

        Mockito.verify(javaMailSender, Mockito.never()).createMimeMessage();
    }

    @Test
    void sendRegistrationEmail_processesTemplateAndSendsMail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));

        Mockito.when(templateEngine.process(
                Mockito.eq("email/send-registration-success"),
                Mockito.any(Context.class)
        )).thenReturn("<html>Welcome!</html>");
        Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendRegistrationEmail(EMAIL, NAME);

        Mockito.verify(templateEngine).process(
                Mockito.eq("email/send-registration-success"),
                Mockito.any(Context.class)
        );
        Mockito.verify(javaMailSender, Mockito.timeout(1000)).send(Mockito.any(MimeMessage.class));
    }
}
