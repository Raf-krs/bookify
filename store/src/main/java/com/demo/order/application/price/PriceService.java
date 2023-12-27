package com.demo.order.application.price;

import com.demo.order.application.policies.DeliveryDiscountPolicy;
import com.demo.order.application.policies.DiscountPolicy;
import com.demo.order.application.policies.TotalPriceDiscountPolicy;
import com.demo.order.domain.Order;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceService {
    private final List<DiscountPolicy> policies = List.of(
            new DeliveryDiscountPolicy(),
            new TotalPriceDiscountPolicy()
    );

    @Transactional
    public OrderPrice calculatePrice(Order order) {
        return new OrderPrice(
                order.getItemsPrice(),
                order.getDeliveryPrice(),
                discounts(order)
        );
    }

    private BigDecimal discounts(Order order) {
        return policies
                .stream()
                .map(strategy -> strategy.calculate(order))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
