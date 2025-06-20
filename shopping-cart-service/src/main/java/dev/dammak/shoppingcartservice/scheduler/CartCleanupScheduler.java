package dev.dammak.shoppingcartservice.scheduler;

import dev.dammak.shoppingcartservice.entity.Cart;
import dev.dammak.shoppingcartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartCleanupScheduler {

    private final CartRepository cartRepository;

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredCarts() {
        log.info("Starting cart cleanup process");

        try {
            LocalDateTime currentTime = LocalDateTime.now();
            List<Cart> expiredCarts = cartRepository.findExpiredCarts(currentTime);

            if (!expiredCarts.isEmpty()) {
                List<Long> cartIds = expiredCarts.stream()
                        .map(Cart::getId)
                        .toList();

                cartRepository.deactivateCarts(cartIds);

                log.info("Deactivated {} expired carts", cartIds.size());
            } else {
                log.info("No expired carts found");
            }
        } catch (Exception e) {
            log.error("Error during cart cleanup: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void logCartStatistics() {
        try {
            Long activeCartCount = cartRepository.countActiveCarts();
            log.info("Cart statistics - Active carts: {}", activeCartCount);
        } catch (Exception e) {
            log.error("Error logging cart statistics: {}", e.getMessage(), e);
        }
    }
}