package dev.dammak.shoppingcartservice.controller;

import dev.dammak.shoppingcartservice.dto.AddItemDto;
import dev.dammak.shoppingcartservice.dto.CartDto;
import dev.dammak.shoppingcartservice.dto.UpdateQuantityDto;
import dev.dammak.shoppingcartservice.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        String effectiveSessionId = sessionId != null ? sessionId : request.getSession().getId();

        CartDto cart;
        if (userId != null) {
            cart = cartService.getCartByUserId(userId);
        } else {
            cart = cartService.getCartBySessionId(effectiveSessionId);
        }

        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @Valid @RequestBody AddItemDto addItemDto,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        String effectiveSessionId = sessionId != null ? sessionId : request.getSession().getId();

        CartDto cart = cartService.addItemToCart(effectiveSessionId, userId, addItemDto);

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItem(
            @PathVariable Long productId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        String effectiveSessionId = sessionId != null ? sessionId : request.getSession().getId();

        CartDto cart = cartService.removeItemFromCart(effectiveSessionId, userId, productId);

        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> updateItemQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateQuantityDto updateQuantityDto,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        String effectiveSessionId = sessionId != null ? sessionId : request.getSession().getId();

        CartDto cart = cartService.updateItemQuantity(effectiveSessionId, userId, productId, updateQuantityDto);

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        String effectiveSessionId = sessionId != null ? sessionId : request.getSession().getId();

        cartService.clearCart(effectiveSessionId, userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<CartDto> mergeCart(
            @RequestParam String guestSessionId,
            @RequestParam Long userId) {

        CartDto cart = cartService.mergeGuestCartWithUserCart(guestSessionId, userId);

        return ResponseEntity.ok(cart);
    }
}