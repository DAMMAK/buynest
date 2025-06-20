package dev.dammak.shoppingcartservice.service;


import dev.dammak.shoppingcartservice.entity.Cart;
import dev.dammak.shoppingcartservice.entity.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PriceCalculationService {

    public void calculateCartTotals(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setSubtotal(subtotal);

        // Calculate discount (can be enhanced with discount rules)
        BigDecimal discountAmount = calculateDiscount(cart);
        cart.setDiscountAmount(discountAmount);

        // Calculate tax on subtotal minus discount
        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = taxableAmount.multiply(cart.getTaxRate())
                .setScale(2, RoundingMode.HALF_UP);
        cart.setTaxAmount(taxAmount);

        // Calculate total
        BigDecimal total = subtotal.subtract(discountAmount).add(taxAmount);
        cart.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateDiscount(Cart cart) {
        // Simple discount calculation - can be enhanced with complex rules
        BigDecimal subtotal = cart.getSubtotal();

        if (subtotal == null) {
            return BigDecimal.ZERO;
        }

        // Example: 10% discount for orders over $100
        if (subtotal.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return subtotal.multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }
}