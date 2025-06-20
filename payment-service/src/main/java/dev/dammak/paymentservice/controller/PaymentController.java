package dev.dammak.paymentservice.controller;


import dev.dammak.paymentservice.dto.PaymentDto;
import dev.dammak.paymentservice.dto.PaymentRequestDto;
import dev.dammak.paymentservice.dto.PaymentStatusDto;
import dev.dammak.paymentservice.dto.RefundDto;
import dev.dammak.paymentservice.service.PaymentService;
import dev.dammak.paymentservice.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @PostMapping("/process")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentDto> processPayment(@Valid @RequestBody PaymentRequestDto request) {
        log.info("Processing payment request for order: {}", request.getOrderId());
        PaymentDto payment = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentStatusDto> getPaymentStatus(@PathVariable String paymentId) {
        PaymentStatusDto status = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDto>> getPaymentHistory(@PathVariable String userId) {
        List<PaymentDto> payments = paymentService.getPaymentHistory(userId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT')")
    public ResponseEntity<RefundDto> processRefund(
            @PathVariable String paymentId,
            @RequestParam @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal amount,
            @RequestParam(required = false) String reason,
            @RequestParam @NotBlank String initiatedBy) {

        RefundDto refund = refundService.processRefund(paymentId, amount, reason, initiatedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(refund);
    }

    @GetMapping("/{paymentId}/refunds")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<RefundDto>> getPaymentRefunds(@PathVariable String paymentId) {
        List<RefundDto> refunds = refundService.getRefundsForPayment(paymentId);
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/refunds/{refundId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RefundDto> getRefundStatus(@PathVariable String refundId) {
        RefundDto refund = refundService.getRefundStatus(refundId);
        return ResponseEntity.ok(refund);
    }

    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> retryFailedPayments() {
        paymentService.retryFailedPayments();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is healthy");
    }
}