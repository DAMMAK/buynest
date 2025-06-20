package dev.dammak.orderservice.dto;

import dev.dammak.orderservice.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusDto {
    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String reason;
    private String trackingNumber;
}