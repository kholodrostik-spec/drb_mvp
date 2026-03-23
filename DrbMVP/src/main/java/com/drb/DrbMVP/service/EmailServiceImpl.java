package com.drb.DrbMVP.service;

import com.drb.DrbMVP.constant.EmailConstants;
import com.drb.DrbMVP.constant.LogMessage;
import com.drb.DrbMVP.dto.notificaation.NotificationDto;
import com.drb.DrbMVP.repository.UserRepository;
import com.drb.DrbMVP.service.service_api.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final Executor executor;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmailAddress;

    public EmailServiceImpl(JavaMailSender javaMailSender,
                            UserRepository userRepository,
                            TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.userRepository = userRepository;
        this.templateEngine = templateEngine;
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public void sendNotificationByEmail(NotificationDto notification, String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            sendEmail(email, notification.getTitle(), notification.getBody());
        } else {
            throw new IllegalArgumentException("User not found by email: " + email);
        }
    }

    private void sendEmail(String receiverEmail, String subject, String content) {
        log.info(LogMessage.IN_SEND_EMAIL, receiverEmail, subject);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        try {
            mimeMessageHelper.setFrom(senderEmailAddress);
            mimeMessageHelper.setTo(receiverEmail);
            mimeMessageHelper.setSubject(subject);
            mimeMessage.setContent(content, EmailConstants.EMAIL_CONTENT_TYPE);
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
        executor.execute(() -> javaMailSender.send(mimeMessage));
    }


    public void sendRegistrationEmail(String email, String name) {
        Context context = new Context();
        context.setVariable("userName", name);
        context.setVariable("userEmail", email);
        context.setVariable("registrationDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        context.setVariable("clientLink", "http://localhost:4200");

        String content = templateEngine.process("email/send-registration-success", context);
        sendEmail(email, "Welcome to DrbMVP!", content);
    }
}
