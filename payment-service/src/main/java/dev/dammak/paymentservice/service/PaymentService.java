package dev.dammak.paymentservice.service;



import dev.dammak.paymentservice.dto.PaymentDto;
import dev.dammak.paymentservice.dto.PaymentRequestDto;
import dev.dammak.paymentservice.dto.PaymentStatusDto;
import dev.dammak.paymentservice.entity.Payment;
import dev.dammak.paymentservice.enums.PaymentStatus;
import dev.dammak.paymentservice.exception.PaymentException;
import dev.dammak.paymentservice.repository.PaymentRepository;
import dev.dammak.paymentservice.util.PaymentUtil;
import dev.dammak.paymentservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripePaymentService stripePaymentService;
    private final PayPalPaymentService payPalPaymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SecurityUtil securityUtil;

    @Transactional
    public PaymentDto processPayment(PaymentRequestDto request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Validate request
        validatePaymentRequest(request);

        // Create payment entity
        Payment payment = createPaymentEntity(request);

        try {
            // Process payment based on method
            Payment processedPayment = processPaymentByMethod(payment, request);

            // Save payment
            Payment savedPayment = paymentRepository.save(processedPayment);

            // Publish payment event
            publishPaymentEvent(savedPayment, "PAYMENT_PROCESSED");

            log.info("Payment processed successfully: {}", savedPayment.getPaymentId());
            return convertToDto(savedPayment);

        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", request.getOrderId(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);

            publishPaymentEvent(payment, "PAYMENT_FAILED");
            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }
    }

    @Retryable(value = {PaymentException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    private Payment processPaymentByMethod(Payment payment, PaymentRequestDto request) {
        return switch (request.getPaymentMethod()) {
            case CREDIT_CARD -> stripePaymentService.processPayment(payment, request);
            case PAYPAL -> payPalPaymentService.processPayment(payment, request);
            case WALLET -> processWalletPayment(payment, request);
            case BANK_TRANSFER -> null;
            case APPLE_PAY -> null;
            case GOOGLE_PAY -> null;
        };
    }

    private Payment processWalletPayment(Payment payment, PaymentRequestDto request) {
        // Implement wallet payment logic
        log.info("Processing wallet payment: {}", payment.getPaymentId());

        // Simulate wallet payment processing
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setGatewayTransactionId("wallet_" + UUID.randomUUID().toString());
        payment.setProcessedAt(LocalDateTime.now());

        return payment;
    }

    public PaymentStatusDto getPaymentStatus(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        return PaymentStatusDto.builder()
                .paymentId(payment.getPaymentId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .build();
    }

    public List<PaymentDto> getPaymentHistory(String userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    public void retryFailedPayments() {
        List<Payment> failedPayments = paymentRepository.findFailedPaymentsForRetry(
                PaymentStatus.FAILED, 3);

        for (Payment payment : failedPayments) {
            try {
                log.info("Retrying payment: {}", payment.getPaymentId());

                payment.setRetryCount(payment.getRetryCount() + 1);
                payment.setStatus(PaymentStatus.PROCESSING);

                // Retry payment processing
                PaymentRequestDto request = createRetryRequest(payment);
                processPaymentByMethod(payment, request);

                paymentRepository.save(payment);
                publishPaymentEvent(payment, "PAYMENT_RETRIED");

            } catch (Exception e) {
                log.error("Payment retry failed: {}", payment.getPaymentId(), e);
                payment.setFailureReason(e.getMessage());
                paymentRepository.save(payment);
            }
        }
    }

    private Payment createPaymentEntity(PaymentRequestDto request) {
        return Payment.builder()
                .paymentId(PaymentUtil.generatePaymentId())
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .retryCount(0)
                .refundedAmount(BigDecimal.ZERO)
                .isFraudulent(false)
                .fraudScore(calculateFraudScore(request))
                .build();
    }

    private BigDecimal calculateFraudScore(PaymentRequestDto request) {
        // Basic fraud detection logic
        BigDecimal score = BigDecimal.ZERO;

        // Check amount threshold
        if (request.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            score = score.add(new BigDecimal("0.3"));
        }

        // Add more fraud detection rules
        return score;
    }

    private void validatePaymentRequest(PaymentRequestDto request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Payment amount must be positive");
        }

        if (request.getOrderId() == null || request.getOrderId().trim().isEmpty()) {
            throw new PaymentException("Order ID is required");
        }

        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new PaymentException("User ID is required");
        }
    }

    private PaymentRequestDto createRetryRequest(Payment payment) {
        return PaymentRequestDto.builder()
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .ipAddress(payment.getIpAddress())
                .userAgent(payment.getUserAgent())
                .build();
    }

    private void publishPaymentEvent(Payment payment, String eventType) {
        PaymentDto paymentDto = convertToDto(payment);
        kafkaTemplate.send("payment-events", eventType, paymentDto);
    }

    private PaymentDto convertToDto(Payment payment) {
        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .failureReason(payment.getFailureReason())
                .refundedAmount(payment.getRefundedAmount())
                .build();
    }
}