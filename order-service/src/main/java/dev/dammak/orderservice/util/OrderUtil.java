package dev.dammak.orderservice.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OrderUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generateOrderNumber() {
        String datePrefix = LocalDateTime.now().format(DATE_FORMATTER);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + datePrefix + "-" + uniqueSuffix;
    }
}