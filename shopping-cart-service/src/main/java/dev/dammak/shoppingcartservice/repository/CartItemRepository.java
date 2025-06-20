package dev.dammak.shoppingcartservice.repository;


import dev.dammak.shoppingcartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId")
    Optional<CartItem> findByCartAndProduct(@Param("cartId") Long cartId, @Param("productId") Long productId);

    void deleteByCartId(Long cartId);
}