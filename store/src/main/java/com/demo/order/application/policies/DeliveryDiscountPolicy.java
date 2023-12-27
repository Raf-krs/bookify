package com.demo.order.application.policies;

import com.demo.order.domain.Order;

import java.math.BigDecimal;

public class DeliveryDiscountPolicy implements DiscountPolicy {

    public static final BigDecimal THRESHOLD = BigDecimal.valueOf(100);

    @Override
    public BigDecimal calculate(Order order) {
        if(order.getItemsPrice().compareTo(THRESHOLD) >= 0) {
            return order.getDeliveryPrice();
        }
        return BigDecimal.ZERO;
    }
}
