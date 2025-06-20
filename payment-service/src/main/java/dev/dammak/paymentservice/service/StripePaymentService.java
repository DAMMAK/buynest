package dev.dammak.paymentservice.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import dev.dammak.paymentservice.dto.PaymentRequestDto;
import dev.dammak.paymentservice.entity.Payment;
import dev.dammak.paymentservice.enums.PaymentStatus;
import dev.dammak.paymentservice.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class StripePaymentService {

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Payment processPayment(Payment payment, PaymentRequestDto request) {
        try {
            log.info("Processing Stripe payment: {}", payment.getPaymentId());

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(payment.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue()) // Convert to cents
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .setDescription("Payment for order: " + payment.getOrderId())
                    .putMetadata("order_id", payment.getOrderId())
                    .putMetadata("user_id", payment.getUserId())
                    .putMetadata("payment_id", payment.getPaymentId())
                    .setConfirm(true)
                    .setReturnUrl("https://your-domain.com/return")
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

//            payment.setGatewayPaymentId(paymentIntent.getId());
//            payment.setGatewayTransactionId(paymentIntent.getCharges().getData().isEmpty() ?
//                    null : paymentIntent.getCharges().getData().get(0).getId());
//            payment.setGatewayResponse(paymentIntent.toJson());
//
//            switch (paymentIntent.getStatus()) {
//                case "succeeded" -> {
//                    payment.setStatus(PaymentStatus.COMPLETED);
//                    payment.setProcessedAt(LocalDateTime.now());
//
//                    // Extract card details if available
//                    if (!paymentIntent.getCharges().getData().isEmpty()) {
//                        var charge = paymentIntent.getCharges().getData().get(0);
//                        var paymentMethodDetails = charge.getPaymentMethodDetails();
//                        if (paymentMethodDetails != null && paymentMethodDetails.getCard() != null) {
//                            payment.setCardLastFour(paymentMethodDetails.getCard().getLast4());
//                            payment.setCardBrand(paymentMethodDetails.getCard().getBrand());
//                        }
//                    }
//                }
//                case "processing" -> payment.setStatus(PaymentStatus.PROCESSING);
//                case "requires_action", "requires_confirmation" -> payment.setStatus(PaymentStatus.PENDING);
//                default -> {
//                    payment.setStatus(PaymentStatus.FAILED);
//                    payment.setFailureReason("Payment failed with status: " + paymentIntent.getStatus());
//                }
//            }

            log.info("Stripe payment processed: {} with status: {}",
                    payment.getPaymentId(), paymentIntent.getStatus());

            return payment;

        } catch (StripeException e) {
            log.error("Stripe payment failed: {}", payment.getPaymentId(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            throw new PaymentException("Stripe payment processing failed: " + e.getMessage());
        }
    }

    public void refundPayment(String paymentIntentId, Long amountCents) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

//        if (!paymentIntent.getLatestCharge().getData().isEmpty()) {
//            String chargeId = paymentIntent.getCharges().getData().get(0).getId();
//
//            Map<String, Object> params = Map.of(
//                    "charge", chargeId,
//                    "amount", amountCents
//            );
//
//            com.stripe.model.Refund.create(params);
//        }
    }
}
