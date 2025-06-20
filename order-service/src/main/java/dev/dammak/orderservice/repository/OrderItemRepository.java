package dev.dammak.orderservice.repository;


import dev.dammak.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductId(Long productId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.userId = :userId")
    List<OrderItem> findByUserId(@Param("userId") Long userId);

    @Query("SELECT oi.productId, SUM(oi.quantity) FROM OrderItem oi " +
            "WHERE oi.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.productId ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalQuantitySoldForProduct(@Param("productId") Long productId);
}