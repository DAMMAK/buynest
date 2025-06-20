package dev.dammak.shoppingcartservice.repository;

import dev.dammak.shoppingcartservice.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findBySessionIdAndIsActiveTrue(String sessionId);

    Optional<Cart> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT c FROM Cart c WHERE c.isActive = true AND c.expiresAt < :currentTime")
    List<Cart> findExpiredCarts(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE Cart c SET c.isActive = false WHERE c.id IN :cartIds")
    void deactivateCarts(@Param("cartIds") List<Long> cartIds);

    @Query("SELECT c FROM Cart c WHERE c.userId IS NULL AND c.sessionId = :sessionId AND c.isActive = true")
    Optional<Cart> findGuestCartBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.isActive = true")
    Long countActiveCarts();
}