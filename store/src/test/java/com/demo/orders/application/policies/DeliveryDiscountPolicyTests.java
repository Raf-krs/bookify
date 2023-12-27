package com.demo.orders.application.policies;

import com.demo.catalog.domain.Book;
import com.demo.order.application.policies.DeliveryDiscountPolicy;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryDiscountPolicyTests {

    private DeliveryDiscountPolicy deliveryDiscountPolicy;

    @BeforeEach
    void setUp() {
        deliveryDiscountPolicy = new DeliveryDiscountPolicy();
    }

    @Test
    void shouldApplyDiscountWhenTotalPriceIsAboveThreshold() {
        // Arrange
        var order = Order.builder()
                         .item(new OrderItem(
                                 getBook("Expensive book", new BigDecimal("110")),
                                 1)
                         ).build();

        // Act
        var response = deliveryDiscountPolicy.calculate(order);

        // Assert
        assertThat(response).isEqualTo(order.getDeliveryPrice());
    }

    @Test
    void shouldNotApplyDiscountWhenTotalPriceIsBelowThreshold() {
        // Arrange
        var order = Order.builder()
                         .item(new OrderItem(
                                    getBook("Cheap book", new BigDecimal("2")),
                                    1)
                         ).build();

        // Act
        var response = deliveryDiscountPolicy.calculate(order);

        // Assert
        assertThat(response).isEqualTo(BigDecimal.ZERO);
    }

    private Book getBook(String title, BigDecimal price) {
        return new Book(title, 1900, price, 1L);
    }
}
