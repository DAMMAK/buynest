package dev.dammak.orderservice.service;

import dev.dammak.orderservice.client.ProductServiceClient;
import dev.dammak.orderservice.dto.*;
import dev.dammak.orderservice.entity.Order;
import dev.dammak.orderservice.entity.OrderItem;
import dev.dammak.orderservice.enums.OrderStatus;
import dev.dammak.orderservice.exception.OrderException;
import dev.dammak.orderservice.repository.OrderRepository;
import dev.dammak.orderservice.util.OrderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderValidationService validationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProductServiceClient productServiceClient;

    public OrderDto createOrder(CreateOrderDto createOrderDto) {

        try{
            log.info("Creating order for user: {}", createOrderDto.getUserId());

            // Validate order
            validationService.validateCreateOrder(createOrderDto);

            // Create order entity
            Order order = buildOrderFromDto(createOrderDto);

            // Save order
            Order savedOrder = orderRepository.save(order);
            // Publish order created event
            publishOrderEvent("order.created", savedOrder);

            log.info("Order created successfully: {}", savedOrder.getOrderNumber());
            return convertToDto(savedOrder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with id: " + orderId));
        return convertToDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException("Order not found with number: " + orderNumber));
        return convertToDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByUserId(String userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> searchOrders(String userId, OrderStatus status,
                                       LocalDateTime startDate, LocalDateTime endDate,
                                       Pageable pageable) {
        Page<Order> orders = orderRepository.findOrdersWithFilters(
                userId, status, startDate, endDate, pageable);
        return orders.map(this::convertToDto);
    }

    public OrderDto updateOrderStatus(Long orderId, OrderStatusDto statusDto) {
        log.info("Updating order status for order: {} to status: {}", orderId, statusDto.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with id: " + orderId));

        // Validate status transition
        validationService.validateStatusTransition(order.getStatus(), statusDto.getStatus());

        // Update order status
        updateOrderStatusFields(order, statusDto);

        Order updatedOrder = orderRepository.save(order);

        // Publish order status updated event
        publishOrderEvent("order.status.updated", updatedOrder);

        log.info("Order status updated successfully: {}", updatedOrder.getOrderNumber());
        return convertToDto(updatedOrder);
    }

    public OrderDto cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with id: " + orderId));

        // Validate cancellation
        validationService.validateCancellation(order);

        // Update order status to cancelled
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        Order cancelledOrder = orderRepository.save(order);

        // Publish order cancelled event
        publishOrderEvent("order.cancelled", cancelledOrder);

        log.info("Order cancelled successfully: {}", cancelledOrder.getOrderNumber());
        return convertToDto(cancelledOrder);
    }

    private Order buildOrderFromDto(CreateOrderDto dto) {
        Order order = Order.builder()
                .orderNumber(OrderUtil.generateOrderNumber())
                .userId(dto.getUserId())
                .status(OrderStatus.PENDING)
                .shippingAddress(dto.getShippingAddress())
                .billingAddress(dto.getBillingAddress())
                .paymentMethod(dto.getPaymentMethod())
                .couponCode(dto.getCouponCode())
                .notes(dto.getNotes())
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .shippingAmount(BigDecimal.ZERO)
                .build();

        // Build order items
        List<OrderItem> orderItems = dto.getOrderItems().stream()
                .map(itemDto -> buildOrderItemFromDto(itemDto, order))
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        // Calculate totals
        calculateOrderTotals(order);

        return order;
    }

    private OrderItem buildOrderItemFromDto(CreateOrderDto.CreateOrderItemDto dto, Order order) {
        // Fetch product details from Product microservice
        ProductDto product = productServiceClient.getProductById(dto.getProductId());
        if (product == null) {
            throw new OrderException("Product not found with id: " + dto.getProductId());
        }

        return OrderItem.builder()
                .order(order)
                .productId(dto.getProductId())
                .productName(product.getName())
                .productSku(product.getSku())
                .productDescription(product.getDescription())
                .productImage(product.getImageUrls().get(0))
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .totalPrice(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity())))
                .discountAmount(BigDecimal.ZERO)
//                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                .build();
    }

    private void calculateOrderTotals(Order order) {
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);

        // Calculate tax (8% for example)
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.08));
        order.setTaxAmount(taxAmount);

        // Calculate shipping (free for orders over $100)
        BigDecimal shippingAmount = subtotal.compareTo(BigDecimal.valueOf(100)) >= 0
                ? BigDecimal.ZERO : BigDecimal.valueOf(10);
        order.setShippingAmount(shippingAmount);

        // Calculate total
        BigDecimal totalAmount = subtotal
                .add(taxAmount)
                .add(shippingAmount)
                .subtract(order.getDiscountAmount());
        order.setTotalAmount(totalAmount);
    }

    private void updateOrderStatusFields(Order order, OrderStatusDto statusDto) {
        OrderStatus newStatus = statusDto.getStatus();
        order.setStatus(newStatus);

        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case SHIPPED:
                order.setShippedAt(now);
                if (statusDto.getTrackingNumber() != null) {
                    order.setNotes(order.getNotes() + "\nTracking: " + statusDto.getTrackingNumber());
                }
                break;
            case DELIVERED:
                order.setDeliveredAt(now);
                break;
            case CANCELLED:
                order.setCancelledAt(now);
                if (statusDto.getReason() != null) {
                    order.setCancellationReason(statusDto.getReason());
                }
                break;
        }
    }

    private void publishOrderEvent(String eventType, Order order) {
        try {
            kafkaTemplate.send("order-events", eventType, convertToDto(order));
        } catch (Exception e) {
            log.error("Failed to publish order event: {}", eventType, e);
        }
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingAmount(order.getShippingAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setCouponCode(order.getCouponCode());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentTransactionId(order.getPaymentTransactionId());
        dto.setNotes(order.getNotes());
        dto.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        dto.setShippedAt(order.getShippedAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getOrderItems() != null) {
            List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                    .map(this::convertItemToDto)
                    .collect(Collectors.toList());
            dto.setOrderItems(itemDtos);
        }

        return dto;
    }

    private OrderItemDto convertItemToDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setProductSku(item.getProductSku());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setProductImage(item.getProductImage());
        dto.setProductDescription(item.getProductDescription());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}