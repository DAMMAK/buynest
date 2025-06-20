package dev.dammak.shoppingcartservice.service;


import dev.dammak.shoppingcartservice.entity.CartItem;
import dev.dammak.shoppingcartservice.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    public Optional<CartItem> findByCartAndProduct(Long cartId, Long productId) {
        return cartItemRepository.findByCartAndProduct(cartId, productId);
    }

    public CartItem save(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    public void delete(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    public void deleteByCartId(Long cartId) {
        cartItemRepository.deleteByCartId(cartId);
    }
}
