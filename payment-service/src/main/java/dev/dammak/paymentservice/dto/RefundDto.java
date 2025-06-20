package dev.dammak.paymentservice.dto;

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
public class RefundDto {
    private String refundId;
    private String paymentId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String reason;
    private String gatewayRefundId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String initiatedBy;
}