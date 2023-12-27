package com.demo.orders.application.services;

import com.demo.catalog.domain.Book;
import com.demo.order.application.RichOrder;
import com.demo.order.application.price.OrderPrice;
import com.demo.order.application.price.PriceService;
import com.demo.order.application.services.QueryOrderService;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderItem;
import com.demo.order.domain.Recipient;
import com.demo.order.infrastructure.OrderRepository;
import com.demo.order.web.OrderRequestParams;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryOrderServiceTests {

    private QueryOrderService queryOrderService;
    private OrderRepository orderRepositoryMock;
    private PriceService priceServiceMock;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        orderRepositoryMock = mock(OrderRepository.class);
        priceServiceMock = mock(PriceService.class);
        queryOrderService = new QueryOrderService(orderRepositoryMock, priceServiceMock);
    }

    @Test
    void findAllShouldReturnValidPage() {
        // Arrange
        var orders = generateOrders(5);
        Page<Order> page = new PageImpl<>(orders, Pageable.unpaged(), orders.size());
        when(orderRepositoryMock.findAllPage(any(Pageable.class))).thenReturn(page);
        OrderRequestParams options = new OrderRequestParams(
                Optional.empty(), Optional.of(1), Optional.of(5));
        when(priceServiceMock.calculatePrice(any(Order.class))).thenReturn(new OrderPrice(
                new BigDecimal("1"), new BigDecimal("0"), new BigDecimal("0")));

        // Act
        Page<RichOrder> result = queryOrderService.findAll(options);

        // Assert
        assertThat(result).hasSize(5);
    }

    // INFO -> Rest of the tests are omitted

    private List<Order> generateOrders(int count) {
        var orders = new ArrayList<Order>();
        for(int i = 0; i < count; i++) {
            orders.add(
                    Order.builder()
                         .item(new OrderItem(
                                 new Book(
                                         faker.book().title(),
                                         faker.number().numberBetween(1700, 2023),
                                         new BigDecimal(faker.number().numberBetween(1, 1000)),
                                         faker.number().numberBetween(0L, 100L)), i
                         ))
                         .recipient(new Recipient())
                         .build()
            );
        }

        return orders;
    }
}
