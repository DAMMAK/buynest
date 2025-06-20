package dev.dammak.paymentservice.entity;

import dev.dammak.paymentservice.enums.PaymentMethod;
import dev.dammak.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String paymentId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    private String gatewayTransactionId;
    private String gatewayPaymentId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    private String failureReason;
    private Integer retryCount;

    @Column(precision = 19, scale = 2)
    private BigDecimal refundedAmount;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Refund> refunds;

    // Fraud detection fields
    private String ipAddress;
    private String userAgent;
    private BigDecimal fraudScore;
    private Boolean isFraudulent;

    // Payment method specific fields
    @Column(length = 4)
    private String cardLastFour;
    private String cardBrand;
    private String paypalEmail;
    private String walletType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime processedAt;

    @Version
    private Long version;
}