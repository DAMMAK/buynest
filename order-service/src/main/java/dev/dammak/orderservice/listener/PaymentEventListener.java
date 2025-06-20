package dev.dammak.orderservice.listener;


import dev.dammak.orderservice.dto.OrderStatusDto;
import dev.dammak.orderservice.enums.OrderStatus;
import dev.dammak.orderservice.service.OrderService;
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
public class PaymentEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    public void handlePaymentEvent(@Payload Map<String, Object> event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String eventType,
                                   Acknowledgment ack) {
        try {
            log.info("Received payment event: {} from topic: {}", eventType, topic);

            switch (eventType) {
                case "payment.completed":
                    handlePaymentCompleted(event);
                    break;
                case "payment.failed":
                    handlePaymentFailed(event);
                    break;
                case "payment.refunded":
                    handlePaymentRefunded(event);
                    break;
                default:
                    log.warn("Unknown payment event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing payment event: {}", eventType, e);
            // Don't acknowledge - let it retry
        }
    }

    private void handlePaymentCompleted(Map<String, Object> event) {
        String orderNumber = (String) event.get("orderNumber");
        String transactionId = (String) event.get("transactionId");

        log.info("Processing payment completed for order: {}", orderNumber);

        try {
            var order = orderService.getOrderByNumber(orderNumber);

            OrderStatusDto statusDto = new OrderStatusDto();
            statusDto.setStatus(OrderStatus.CONFIRMED);
            statusDto.setReason("Payment completed successfully");

            orderService.updateOrderStatus(order.getId(), statusDto);

            log.info("Order status updated to CONFIRMED for order: {}", orderNumber);
        } catch (Exception e) {
            log.error("Failed to update order status for payment completion: {}", orderNumber, e);
            throw e;
        }
    }

    private void handlePaymentFailed(Map<String, Object> event) {
        String orderNumber = (String) event.get("orderNumber");
        String reason = (String) event.get("reason");

        log.info("Processing payment failed for order: {}", orderNumber);

        try {
            var order = orderService.getOrderByNumber(orderNumber);
            orderService.cancelOrder(order.getId(), "Payment failed: " + reason);

            log.info("Order cancelled due to payment failure: {}", orderNumber);
        } catch (Exception e) {
            log.error("Failed to cancel order for payment failure: {}", orderNumber, e);
            throw e;
        }
    }

    private void handlePaymentRefunded(Map<String, Object> event) {
        String orderNumber = (String) event.get("orderNumber");

        log.info("Processing payment refunded for order: {}", orderNumber);

        try {
            var order = orderService.getOrderByNumber(orderNumber);

            OrderStatusDto statusDto = new OrderStatusDto();
            statusDto.setStatus(OrderStatus.REFUNDED);
            statusDto.setReason("Payment refunded");

            orderService.updateOrderStatus(order.getId(), statusDto);

            log.info("Order status updated to REFUNDED for order: {}", orderNumber);
        } catch (Exception e) {
            log.error("Failed to update order status for refund: {}", orderNumber, e);
            throw e;
        }
    }
}