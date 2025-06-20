package dev.dammak.shoppingcartservice.util;


import dev.dammak.shoppingcartservice.dto.CartDto;
import dev.dammak.shoppingcartservice.dto.CartItemDto;
import dev.dammak.shoppingcartservice.entity.Cart;
import dev.dammak.shoppingcartservice.entity.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public class CartUtil {

    public static CartDto convertToDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(CartUtil::convertToDto)
                .collect(Collectors.toList());

        return CartDto.builder()
                .id(cart.getId())
                .sessionId(cart.getSessionId())
                .userId(cart.getUserId())
                .items(itemDtos)
                .subtotal(cart.getSubtotal())
                .taxAmount(cart.getTaxAmount())
                .discountAmount(cart.getDiscountAmount())
                .total(cart.getTotal())
                .taxRate(cart.getTaxRate())
                .isActive(cart.getIsActive())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .expiresAt(cart.getExpiresAt())
                .totalItems(itemDtos.size())
                .build();
    }

    public static CartItemDto convertToDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        return CartItemDto.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .productSku(cartItem.getProductSku())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getTotalPrice())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}