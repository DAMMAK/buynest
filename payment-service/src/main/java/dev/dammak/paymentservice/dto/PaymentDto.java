package dev.dammak.paymentservice.dto;

import dev.dammak.paymentservice.enums.PaymentMethod;
import dev.dammak.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String gatewayTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String failureReason;
    private BigDecimal refundedAmount;
    private String cardLastFour;
    private String cardBrand;
}