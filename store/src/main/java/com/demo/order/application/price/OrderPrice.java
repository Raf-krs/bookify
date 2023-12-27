package com.demo.order.application.price;

import java.math.BigDecimal;

public record OrderPrice(BigDecimal itemsPrice, BigDecimal deliveryPrice, BigDecimal discounts) {
    public BigDecimal finalPrice() {
        return itemsPrice.add(deliveryPrice).subtract(discounts);
    }
}
