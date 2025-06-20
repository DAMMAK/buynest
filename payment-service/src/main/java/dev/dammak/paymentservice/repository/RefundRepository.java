package dev.dammak.paymentservice.repository;

import dev.dammak.paymentservice.entity.Refund;
import dev.dammak.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByRefundId(String refundId);

    List<Refund> findByPaymentId(Long paymentId);

    List<Refund> findByStatus(PaymentStatus status);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = 'COMPLETED'")
    BigDecimal getTotalRefundedAmountForPayment(@Param("paymentId") Long paymentId);

    @Query("SELECT r FROM Refund r WHERE r.payment.paymentId = :paymentId")
    List<Refund> findByPaymentPaymentId(@Param("paymentId") String paymentId);
}