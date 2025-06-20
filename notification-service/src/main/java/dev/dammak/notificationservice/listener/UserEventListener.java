package dev.dammak.notificationservice.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dammak.notificationservice.dto.NotificationDto;
import dev.dammak.notificationservice.enums.NotificationType;
import dev.dammak.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-registered", groupId = "notification-service")
    public void handleUserRegistered(String message, Acknowledgment acknowledgment) {
        try {
            JsonNode userEvent = objectMapper.readTree(message);

            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName", userEvent.get("firstName").asText());
            variables.put("lastName", userEvent.get("lastName").asText());
            variables.put("email", userEvent.get("email").asText());

            NotificationDto notification = NotificationDto.builder()
                    .userId(userEvent.get("userId").asText())
                    .recipient(userEvent.get("email").asText())
                    .type(NotificationType.EMAIL)
                    .templateId("welcome-email")
                    .subject("Welcome to Our E-commerce Platform!")
                    .variables(variables)
                    .build();

            notificationService.sendNotification(notification);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process user registered event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "password-reset-requested", groupId = "notification-service")
    public void handlePasswordResetRequested(String message, Acknowledgment acknowledgment) {
        try {
            JsonNode resetEvent = objectMapper.readTree(message);

            Map<String, Object> variables = new HashMap<>();
            variables.put("resetToken", resetEvent.get("resetToken").asText());
            variables.put("expiryTime", resetEvent.get("expiryTime").asText());

            NotificationDto notification = NotificationDto.builder()
                    .userId(resetEvent.get("userId").asText())
                    .recipient(resetEvent.get("email").asText())
                    .type(NotificationType.EMAIL)
                    .templateId("password-reset")
                    .subject("Password Reset Request")
                    .variables(variables)
                    .build();

            notificationService.sendNotification(notification);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process password reset event: {}", e.getMessage(), e);
        }
    }
}