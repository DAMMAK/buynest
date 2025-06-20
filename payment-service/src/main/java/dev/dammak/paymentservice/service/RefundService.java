package dev.dammak.paymentservice.service;

import dev.dammak.paymentservice.dto.RefundDto;
import dev.dammak.paymentservice.entity.Payment;
import dev.dammak.paymentservice.entity.Refund;
import dev.dammak.paymentservice.enums.PaymentMethod;
import dev.dammak.paymentservice.enums.PaymentStatus;
import dev.dammak.paymentservice.exception.PaymentException;
import dev.dammak.paymentservice.repository.PaymentRepository;
import dev.dammak.paymentservice.repository.RefundRepository;
import dev.dammak.paymentservice.util.PaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final StripePaymentService stripePaymentService;
    private final PayPalPaymentService payPalPaymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public RefundDto processRefund(String paymentId, BigDecimal amount, String reason, String initiatedBy) {
        log.info("Processing refund for payment: {}, amount: {}", paymentId, amount);

        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        validateRefundRequest(payment, amount);

        Refund refund = createRefundEntity(payment, amount, reason, initiatedBy);

        try {
            // Process refund with gateway
            processRefundWithGateway(payment, refund);

            // Save refund
            Refund savedRefund = refundRepository.save(refund);

            // Update payment
            updatePaymentForRefund(payment, amount);

            // Publish refund event
            publishRefundEvent(savedRefund, "REFUND_PROCESSED");

            log.info("Refund processed successfully: {}", savedRefund.getRefundId());
            return convertToDto(savedRefund);

        } catch (Exception e) {
            log.error("Refund processing failed for payment: {}", paymentId, e);
            refund.setStatus(PaymentStatus.FAILED);
            refund.setFailureReason(e.getMessage());
            refundRepository.save(refund);

            publishRefundEvent(refund, "REFUND_FAILED");
            throw new PaymentException("Refund processing failed: " + e.getMessage());
        }
    }

    public List<RefundDto> getRefundsForPayment(String paymentId) {
        List<Refund> refunds = refundRepository.findByPaymentPaymentId(paymentId);
        return refunds.stream()
                .map(this::convertToDto)
                .toList();
    }

    public RefundDto getRefundStatus(String refundId) {
        Refund refund = refundRepository.findByRefundId(refundId)
                .orElseThrow(() -> new PaymentException("Refund not found: " + refundId));

        return convertToDto(refund);
    }

    private void processRefundWithGateway(Payment payment, Refund refund) {
        try {
            if (payment.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
                // Process Stripe refund
                long amountCents = refund.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
                stripePaymentService.refundPayment(payment.getGatewayPaymentId(), amountCents);

            } else if (payment.getPaymentMethod() == PaymentMethod.PAYPAL) {
                // Process PayPal refund
                payPalPaymentService.refundPayment(payment.getGatewayTransactionId(),
                        refund.getAmount().toString());
            }

            refund.setStatus(PaymentStatus.COMPLETED);
            refund.setProcessedAt(LocalDateTime.now());
            refund.setGatewayRefundId("REFUND_" + System.currentTimeMillis());

        } catch (Exception e) {
            log.error("Gateway refund failed for payment: {}", payment.getPaymentId(), e);
            throw new PaymentException("Gateway refund failed: " + e.getMessage());
        }
    }

    private void validateRefundRequest(Payment payment, BigDecimal amount) {
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot refund payment that is not completed");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Refund amount must be positive");
        }

        BigDecimal totalRefunded = refundRepository.getTotalRefundedAmountForPayment(payment.getId());
        if (totalRefunded == null) {
            totalRefunded = BigDecimal.ZERO;
        }

        BigDecimal availableForRefund = payment.getAmount().subtract(totalRefunded);
        if (amount.compareTo(availableForRefund) > 0) {
            throw new PaymentException("Refund amount exceeds available amount for refund");
        }
    }

    private Refund createRefundEntity(Payment payment, BigDecimal amount, String reason, String initiatedBy) {
        return Refund.builder()
                .refundId(PaymentUtil.generateRefundId())
                .payment(payment)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .reason(reason)
                .initiatedBy(initiatedBy)
                .build();
    }

    private void updatePaymentForRefund(Payment payment, BigDecimal refundAmount) {
        BigDecimal currentRefunded = payment.getRefundedAmount() != null ?
                payment.getRefundedAmount() : BigDecimal.ZERO;
        payment.setRefundedAmount(currentRefunded.add(refundAmount));

        if (payment.getRefundedAmount().compareTo(payment.getAmount()) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
        }

        paymentRepository.save(payment);
    }

    private void publishRefundEvent(Refund refund, String eventType) {
        RefundDto refundDto = convertToDto(refund);
        kafkaTemplate.send("payment-events", eventType, refundDto);
    }

    private RefundDto convertToDto(Refund refund) {
        return RefundDto.builder()
                .refundId(refund.getRefundId())
                .paymentId(refund.getPayment().getPaymentId())
                .amount(refund.getAmount())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .gatewayRefundId(refund.getGatewayRefundId())
                .createdAt(refund.getCreatedAt())
                .processedAt(refund.getProcessedAt())
                .initiatedBy(refund.getInitiatedBy())
                .build();
    }
}