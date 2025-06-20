package dev.dammak.orderservice.service;

import dev.dammak.orderservice.enums.OrderStatus;
import dev.dammak.orderservice.repository.OrderItemRepository;
import dev.dammak.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderAnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Cacheable(value = "orderAnalytics", key = "#startDate.toString() + '_' + #endDate.toString()")
    public Map<String, Object> getOrderAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();

        // Total revenue
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatusAndDateRange(
                OrderStatus.DELIVERED, startDate, endDate);
        analytics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // Order counts by status
        Map<OrderStatus, Long> orderCountsByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.findByCreatedAtBetween(startDate, endDate)
                    .stream()
                    .filter(order -> order.getStatus() == status)
                    .count();
            orderCountsByStatus.put(status, count);
        }
        analytics.put("orderCountsByStatus", orderCountsByStatus);

        // Top selling products
        List<Object[]> topProducts = orderItemRepository.findTopSellingProducts(startDate, endDate);
        analytics.put("topSellingProducts", topProducts);

        return analytics;
    }

    @Cacheable(value = "userOrderStats", key = "#userId")
    public Map<String, Object> getUserOrderStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();

        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByUserIdAndStatus(userId, status);
            stats.put(status.name().toLowerCase() + "Count", count);
        }

        return stats;
    }
}