package dev.dammak.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderDto {

    @NotNull(message = "User ID is required")
    private String userId;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<CreateOrderItemDto> orderItems;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;

    @NotBlank(message = "Billing address is required")
    @Size(max = 500, message = "Billing address cannot exceed 500 characters")
    private String billingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @Size(max = 50, message = "Coupon code cannot exceed 50 characters")
    private String couponCode;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Data
    public static class CreateOrderItemDto {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity cannot exceed 999")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be positive")
        private BigDecimal unitPrice;
    }
}