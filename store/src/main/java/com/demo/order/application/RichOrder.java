package com.demo.order.application;

import com.demo.order.application.price.OrderPrice;
import com.demo.order.domain.OrderItem;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.Recipient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record RichOrder(
        Long id,
        OrderStatus status,
        Set<OrderItem> items,
        Recipient recipient,
        LocalDateTime createdAt,
        OrderPrice orderPrice,
        BigDecimal finalPrice) {
}
