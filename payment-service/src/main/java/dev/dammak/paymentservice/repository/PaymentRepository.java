package dev.dammak.paymentservice.repository;

import dev.dammak.paymentservice.entity.Payment;
import dev.dammak.paymentservice.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByUserId(String userId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime dateTime);

    Page<Payment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.retryCount < :maxRetries")
    List<Payment> findFailedPaymentsForRetry(@Param("status") PaymentStatus status,
                                             @Param("maxRetries") Integer maxRetries);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    Long getPaymentCountBetweenDates(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.isFraudulent = true OR p.fraudScore > :threshold")
    List<Payment> findSuspiciousPayments(@Param("threshold") BigDecimal threshold);
}