package dev.dammak.paymentservice.listener;

import dev.dammak.paymentservice.dto.PaymentRequestDto;
import dev.dammak.paymentservice.enums.PaymentMethod;
import dev.dammak.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderEvent(@Payload Map<String, Object> orderEvent,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                 Acknowledgment acknowledgment) {

        try {
            log.info("Received order event: {}", key);

            if ("ORDER_CONFIRMED".equals(key)) {
                processOrderConfirmedEvent(orderEvent);
            } else if ("ORDER_CANCELLED".equals(key)) {
                processOrderCancelledEvent(orderEvent);
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing order event: {}", key, e);
            // Don't acknowledge on error to trigger retry
        }
    }

    private void processOrderConfirmedEvent(Map<String, Object> orderEvent) {
        log.info("Processing order confirmed event");

        // Extract order details and create payment request
        PaymentRequestDto paymentRequest = PaymentRequestDto.builder()
                .orderId(String.valueOf(orderEvent.get("orderId")))
                .userId(String.valueOf(orderEvent.get("userId")))
                .amount(new java.math.BigDecimal(String.valueOf(orderEvent.get("amount"))))
                .currency(String.valueOf(orderEvent.get("currency")))
                .paymentMethod(PaymentMethod.valueOf(String.valueOf(orderEvent.get("paymentMethod"))))
                .build();

        paymentService.processPayment(paymentRequest);
    }

    private void processOrderCancelledEvent(Map<String, Object> orderEvent) {
        log.info("Processing order cancelled event");

        // Handle order cancellation logic
        String orderId = String.valueOf(orderEvent.get("orderId"));

        // Find and refund payment if exists
        // Implementation depends on business logic
    }
}