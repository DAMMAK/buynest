package dev.dammak.orderservice.service;



import dev.dammak.orderservice.dto.CreateOrderDto;
import dev.dammak.orderservice.entity.Order;
import dev.dammak.orderservice.enums.OrderStatus;
import dev.dammak.orderservice.exception.OrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderValidationService {

    private static final Set<OrderStatus> CANCELLABLE_STATUSES = EnumSet.of(
            OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PROCESSING
    );

    public void validateCreateOrder(CreateOrderDto createOrderDto) {
        if (createOrderDto.getOrderItems().isEmpty()) {
            throw new OrderException("Order must contain at least one item");
        }

        // Validate each order item
        createOrderDto.getOrderItems().forEach(this::validateOrderItem);

        // Additional business validations can be added here
        log.debug("Order validation passed for user: {}", createOrderDto.getUserId());
    }

    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new OrderException("Order is already in " + newStatus + " status");
        }

        boolean validTransition = switch (currentStatus) {
            case PENDING -> EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED).contains(newStatus);
            case CONFIRMED -> EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED).contains(newStatus);
            case PROCESSING -> EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED).contains(newStatus);
            case SHIPPED -> EnumSet.of(OrderStatus.DELIVERED, OrderStatus.RETURNED).contains(newStatus);
            case DELIVERED -> EnumSet.of(OrderStatus.RETURNED).contains(newStatus);
            case CANCELLED -> EnumSet.of(OrderStatus.REFUNDED).contains(newStatus);
            case RETURNED -> EnumSet.of(OrderStatus.REFUNDED).contains(newStatus);
            case REFUNDED -> false; // Terminal state
        };

        if (!validTransition) {
            throw new OrderException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }

        log.debug("Status transition validation passed: {} -> {}", currentStatus, newStatus);
    }

    public void validateCancellation(Order order) {
        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new OrderException(
                    String.format("Order with status %s cannot be cancelled", order.getStatus())
            );
        }

        log.debug("Cancellation validation passed for order: {}", order.getOrderNumber());
    }

    private void validateOrderItem(CreateOrderDto.CreateOrderItemDto item) {
        if (item.getQuantity() <= 0) {
            throw new OrderException("Order item quantity must be positive");
        }

        if (item.getUnitPrice().signum() <= 0) {
            throw new OrderException("Order item unit price must be positive");
        }

        // Additional item validations can be added here
        // e.g., inventory check, product existence validation
    }
}