package dev.dammak.shoppingcartservice.service;


import dev.dammak.shoppingcartservice.dto.AddItemDto;
import dev.dammak.shoppingcartservice.dto.CartDto;
import dev.dammak.shoppingcartservice.dto.UpdateQuantityDto;
import dev.dammak.shoppingcartservice.entity.Cart;
import dev.dammak.shoppingcartservice.entity.CartItem;
import dev.dammak.shoppingcartservice.exception.CartException;
import dev.dammak.shoppingcartservice.repository.CartRepository;
import dev.dammak.shoppingcartservice.util.CartUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;
    private final PriceCalculationService priceCalculationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Cacheable(value = "carts", key = "#sessionId")
    public CartDto getCartBySessionId(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new CartException("Cart not found for session: " + sessionId));

        return CartUtil.convertToDto(cart);
    }

    @Cacheable(value = "carts", key = "'user_' + #userId")
    public CartDto getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new CartException("Cart not found for user: " + userId));

        return CartUtil.convertToDto(cart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartDto addItemToCart(String sessionId, Long userId, AddItemDto addItemDto) {
        Cart cart = getOrCreateCart(sessionId, userId);

        Optional<CartItem> existingItem = cartItemService.findByCartAndProduct(cart.getId(), addItemDto.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + addItemDto.getQuantity());
            cartItemService.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(addItemDto.getProductId())
                    .productName(addItemDto.getProductName())
                    .productSku(addItemDto.getProductSku())
                    .quantity(addItemDto.getQuantity())
                    .unitPrice(addItemDto.getUnitPrice())
                    .build();
            cart.addItem(newItem);
        }

        priceCalculationService.calculateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setExpiresAt(LocalDateTime.now().plusDays(7)); // Reset expiration

        Cart savedCart = cartRepository.save(cart);

        // Publish Kafka event
        publishCartEvent("ITEM_ADDED", savedCart, addItemDto);

        log.info("Item added to cart. SessionId: {}, ProductId: {}, Quantity: {}",
                sessionId, addItemDto.getProductId(), addItemDto.getQuantity());

        return CartUtil.convertToDto(savedCart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartDto removeItemFromCart(String sessionId, Long userId, Long productId) {
        Cart cart = getOrCreateCart(sessionId, userId);

        CartItem item = cartItemService.findByCartAndProduct(cart.getId(), productId)
                .orElseThrow(() -> new CartException("Item not found in cart"));

        cart.removeItem(item);
        cartItemService.delete(item);

        priceCalculationService.calculateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        // Publish Kafka event
        publishCartEvent("ITEM_REMOVED", savedCart, productId);

        log.info("Item removed from cart. SessionId: {}, ProductId: {}", sessionId, productId);

        return CartUtil.convertToDto(savedCart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartDto updateItemQuantity(String sessionId, Long userId, Long productId, UpdateQuantityDto updateDto) {
        Cart cart = getOrCreateCart(sessionId, userId);

        CartItem item = cartItemService.findByCartAndProduct(cart.getId(), productId)
                .orElseThrow(() -> new CartException("Item not found in cart"));

        item.setQuantity(updateDto.getQuantity());
        cartItemService.save(item);

        priceCalculationService.calculateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        // Publish Kafka event
        publishCartEvent("ITEM_UPDATED", savedCart, updateDto);

        log.info("Item quantity updated. SessionId: {}, ProductId: {}, NewQuantity: {}",
                sessionId, productId, updateDto.getQuantity());

        return CartUtil.convertToDto(savedCart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public void clearCart(String sessionId, Long userId) {
        Cart cart = getOrCreateCart(sessionId, userId);

        cart.getItems().clear();
        priceCalculationService.calculateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);

        // Publish Kafka event
        publishCartEvent("CART_CLEARED", cart, null);

        log.info("Cart cleared. SessionId: {}", sessionId);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartDto mergeGuestCartWithUserCart(String guestSessionId, Long userId) {
        Optional<Cart> guestCart = cartRepository.findGuestCartBySessionId(guestSessionId);
        Optional<Cart> userCart = cartRepository.findByUserIdAndIsActiveTrue(userId);

        if (guestCart.isEmpty()) {
            return userCart.map(CartUtil::convertToDto)
                    .orElseThrow(() -> new CartException("No cart found for user"));
        }

        Cart targetCart = userCart.orElseGet(() -> createNewCart(null, userId));
        Cart sourceCart = guestCart.get();

        // Merge items from guest cart to user cart
        for (CartItem guestItem : sourceCart.getItems()) {
            Optional<CartItem> existingItem = cartItemService.findByCartAndProduct(
                    targetCart.getId(), guestItem.getProductId());

            if (existingItem.isPresent()) {
                CartItem existing = existingItem.get();
                existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
                cartItemService.save(existing);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(targetCart)
                        .productId(guestItem.getProductId())
                        .productName(guestItem.getProductName())
                        .productSku(guestItem.getProductSku())
                        .quantity(guestItem.getQuantity())
                        .unitPrice(guestItem.getUnitPrice())
                        .build();
                targetCart.addItem(newItem);
            }
        }

        // Deactivate guest cart
        sourceCart.setIsActive(false);
        cartRepository.save(sourceCart);

        priceCalculationService.calculateCartTotals(targetCart);
        targetCart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(targetCart);

        // Publish Kafka event
        publishCartEvent("CART_MERGED", savedCart, guestSessionId);

        log.info("Guest cart merged with user cart. GuestSessionId: {}, UserId: {}", guestSessionId, userId);

        return CartUtil.convertToDto(savedCart);
    }

    private Cart getOrCreateCart(String sessionId, Long userId) {
        if (userId != null) {
            return cartRepository.findByUserIdAndIsActiveTrue(userId)
                    .orElseGet(() -> createNewCart(sessionId, userId));
        } else {
            return cartRepository.findBySessionIdAndIsActiveTrue(sessionId)
                    .orElseGet(() -> createNewCart(sessionId, null));
        }
    }

    private Cart createNewCart(String sessionId, Long userId) {
        String cartSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

        Cart cart = Cart.builder()
                .sessionId(cartSessionId)
                .userId(userId)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        priceCalculationService.calculateCartTotals(cart);

        return cartRepository.save(cart);
    }

    private void publishCartEvent(String eventType, Cart cart, Object data) {
        try {
            CartEvent event = CartEvent.builder()
                    .eventType(eventType)
                    .cartId(cart.getId())
                    .sessionId(cart.getSessionId())
                    .userId(cart.getUserId())
                    .timestamp(LocalDateTime.now())
                    .data(data)
                    .build();

            kafkaTemplate.send("cart-events", event);
        } catch (Exception e) {
            log.error("Failed to publish cart event: {}", e.getMessage(), e);
        }
    }

    // Inner class for Kafka events
    @lombok.Data
    @lombok.Builder
    public static class CartEvent {
        private String eventType;
        private Long cartId;
        private String sessionId;
        private Long userId;
        private LocalDateTime timestamp;
        private Object data;
    }
}