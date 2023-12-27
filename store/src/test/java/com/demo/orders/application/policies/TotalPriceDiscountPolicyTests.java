package com.demo.orders.application.policies;

import com.demo.catalog.domain.Book;
import com.demo.order.application.policies.TotalPriceDiscountPolicy;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TotalPriceDiscountPolicyTests {

    private TotalPriceDiscountPolicy totalPriceDiscountPolicy;

    @BeforeEach
    void setUp() {
        totalPriceDiscountPolicy = new TotalPriceDiscountPolicy();
    }

    @Test
    void shouldApplyDiscountWhenTotalPriceIsAboveOrEqual400() {
        // Arrange
        var lowestBookPrice = new BigDecimal("49.99");
        var order = Order.builder()
                         .item(new OrderItem(
                                 getBook("Expensive book", new BigDecimal("110.80")),
                                 3)
                         )
                         .item(new OrderItem(
                                getBook("Regular book", lowestBookPrice),
                                2)
                        ).build();

        // Act
        var response = totalPriceDiscountPolicy.calculate(order);

        // Assert
        assertThat(response).isEqualTo(lowestBookPrice);
    }

    @Test
    void shouldApplyDiscountWhenTotalPriceIsAboveOrEquals200() {
        // Arrange
        var bookPrice = new BigDecimal("110.80");
        var order = Order.builder()
                         .item(new OrderItem(
                                 getBook("Regular book", bookPrice),
                                 2)
                         ).build();

        // Act
        var response = totalPriceDiscountPolicy.calculate(order);

        // Assert
        assertThat(response).isEqualTo(bookPrice.divide(BigDecimal.valueOf(2)));
    }

    @Test
    void shouldNotApplyDiscountWhenTotalPriceIsUnder200() {
        // Arrange
        var order = Order.builder()
                         .item(new OrderItem(
                                 getBook("Cheap book", new BigDecimal("9.90")),
                                 1)
                         ).build();

        // Act
        var response = totalPriceDiscountPolicy.calculate(order);

        // Assert
        assertThat(response).isEqualTo(BigDecimal.ZERO);
    }

    private Book getBook(String title, BigDecimal price) {
        return new Book(title, 1900, price, 1L);
    }
}
