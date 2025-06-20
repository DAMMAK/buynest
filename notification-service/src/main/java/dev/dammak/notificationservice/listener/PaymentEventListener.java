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
public class PaymentEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-successful", groupId = "notification-service")
    public void handlePaymentSuccessful(String message, Acknowledgment acknowledgment) {
        try {
            JsonNode paymentEvent = objectMapper.readTree(message);

            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", paymentEvent.get("orderId").asText());
            variables.put("amount", paymentEvent.get("amount").asDouble());
            variables.put("paymentMethod", paymentEvent.get("paymentMethod").asText());
            variables.put("transactionId", paymentEvent.get("transactionId").asText());

            NotificationDto notification = NotificationDto.builder()
                    .userId(paymentEvent.get("customerId").asText())
                    .recipient(paymentEvent.get("customerEmail").asText())
                    .type(NotificationType.EMAIL)
                    .templateId("payment-confirmation")
                    .subject("Payment Confirmation - " + paymentEvent.get("orderId").asText())
                    .variables(variables)
                    .build();

            notificationService.sendNotification(notification);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process payment successful event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "notification-service")
    public void handlePaymentFailed(String message, Acknowledgment acknowledgment) {
        try {
            JsonNode paymentEvent = objectMapper.readTree(message);

            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", paymentEvent.get("orderId").asText());
            variables.put("amount", paymentEvent.get("amount").asDouble());
            variables.put("failureReason", paymentEvent.get("failureReason").asText());

            NotificationDto notification = NotificationDto.builder()
                    .userId(paymentEvent.get("customerId").asText())
                    .recipient(paymentEvent.get("customerEmail").asText())
                    .type(NotificationType.EMAIL)
                    .templateId("payment-failed")
                    .subject("Payment Failed - " + paymentEvent.get("orderId").asText())
                    .variables(variables)
                    .build();

            notificationService.sendNotification(notification);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process payment failed event: {}", e.getMessage(), e);
        }
    }
}