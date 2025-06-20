package dev.dammak.orderservice.dto;


import dev.dammak.orderservice.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderDto {
    private Long id;
    private String orderNumber;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private String couponCode;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private String paymentTransactionId;
    private String notes;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private List<OrderItemDto> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}