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
public class OrderEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "notification-service")
    public void handleOrderCreated(String message, Acknowledgment acknowledgment) {
        try {
            JsonNode orderEvent = objectMapper.readTree(message);

            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderEvent.get("orderId").asText());
            variables.put("customerName", orderEvent.get("customerName").asText());
            variables.put("totalAmount", orderEvent.get("totalAmount").asDouble());
            variables.put("orderDate", orderEvent.get("orderDate").asText());

            NotificationDto notification = NotificationDto.builder()
                    .userId(orderEvent.get("customerId").asText())
                    .recipient(orderEvent.get("customerEmail").asText())
                    .type(NotificationType.EMAIL)
                    .templateId("order-confirmation")
                    .subject("Order Confirmation - " + orderEvent.get("orderId").asText())
                    .variables(variables)
                    .build();

            notificationService.sendNotification(notification);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process order created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-shipped", groupId = "notification-service")
    public void handleOrderShipped(String message, Acknowledgment acknowledgment) {
        try {
            JsonNode orderEvent = objectMapper.readTree(message);

            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderEvent.get("orderId").asText());
            variables.put("trackingNumber", orderEvent.get("trackingNumber").asText());
            variables.put("estimatedDelivery", orderEvent.get("estimatedDelivery").asText());

            // Send email notification
            NotificationDto emailNotification = NotificationDto.builder()
                    .userId(orderEvent.get("customerId").asText())
                    .recipient(orderEvent.get("customerEmail").asText())
                    .type(NotificationType.EMAIL)
                    .templateId("shipping-update")
                    .subject("Your Order Has Shipped - " + orderEvent.get("orderId").asText())
                    .variables(variables)
                    .build();

            // Send SMS notification
            NotificationDto smsNotification = NotificationDto.builder()
                    .userId(orderEvent.get("customerId").asText())
                    .recipient(orderEvent.get("customerPhone").asText())
                    .type(NotificationType.SMS)
                    .content("Your order " + orderEvent.get("orderId").asText() +
                            " has shipped! Tracking: " + orderEvent.get("trackingNumber").asText())
                    .variables(variables)
                    .build();

            notificationService.sendNotification(emailNotification);
            notificationService.sendNotification(smsNotification);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process order shipped event: {}", e.getMessage(), e);
        }
    }
}