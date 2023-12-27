package com.demo.order.application.policies;

import com.demo.order.domain.Order;
import com.demo.order.domain.OrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class TotalPriceDiscountPolicy implements DiscountPolicy {

    @Override
    public BigDecimal calculate(Order order) {
        if(isGreaterOrEqual(order, 400)) {
            return lowestBookPrice(order.getItems());
        } else if(isGreaterOrEqual(order, 200)) {
            BigDecimal lowestPrice = lowestBookPrice(order.getItems());
            return lowestPrice.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal lowestBookPrice(Set<OrderItem> items) {
        return items.stream()
                    .map(item -> item.getBook().getPrice().toBigDecimal())
                    .sorted()
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
    }

    private boolean isGreaterOrEqual(Order order, int value) {
        return order.getItemsPrice()
                    .compareTo(BigDecimal.valueOf(value)) >= 0;
    }
}
