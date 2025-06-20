package dev.dammak.paymentservice.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentUtil {

    private static final String PAYMENT_PREFIX = "PAY";
    private static final String REFUND_PREFIX = "REF";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generatePaymentId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%04d", RANDOM.nextInt(10000));
        return PAYMENT_PREFIX + timestamp + randomSuffix;
    }

    public static String generateRefundId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%04d", RANDOM.nextInt(10000));
        return REFUND_PREFIX + timestamp + randomSuffix;
    }

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public static boolean isValidCurrency(String currency) {
        return currency != null && currency.length() == 3 && currency.matches("[A-Z]{3}");
    }
}