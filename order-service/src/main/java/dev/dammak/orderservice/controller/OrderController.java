package dev.dammak.orderservice.controller;


import dev.dammak.orderservice.dto.CreateOrderDto;
import dev.dammak.orderservice.dto.OrderDto;
import dev.dammak.orderservice.dto.OrderStatusDto;
import dev.dammak.orderservice.enums.OrderStatus;
import dev.dammak.orderservice.service.OrderAnalyticsService;
import dev.dammak.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderAnalyticsService analyticsService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
        log.info("Creating order for user: {}", createOrderDto.getUserId());
        OrderDto orderDto = orderService.createOrder(createOrderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDto orderDto = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderDto>> getOrdersByUserId(
            @PathVariable String userId,
            Pageable pageable) {
        Page<OrderDto> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OrderDto>> searchOrders(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<OrderDto> orders = orderService.searchOrders(userId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusDto statusDto) {
        log.info("Updating status for order: {} to {}", orderId, statusDto.getStatus());
        OrderDto orderDto = orderService.updateOrderStatus(orderId, statusDto);
        return ResponseEntity.ok(orderDto);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling order: {}", orderId);
        OrderDto orderDto = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getOrderAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> analytics = analyticsService.getOrderAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserOrderStatistics(@PathVariable String userId) {
        Map<String, Object> stats = analyticsService.getUserOrderStatistics(userId);
        return ResponseEntity.ok(stats);
    }
}