package com.demo.order.application.policies;

import com.demo.order.domain.Order;

import java.math.BigDecimal;

public interface DiscountPolicy {
    BigDecimal calculate(Order order);
}
