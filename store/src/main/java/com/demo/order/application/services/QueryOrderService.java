package com.demo.order.application.services;

import com.demo.order.application.RichOrder;
import com.demo.order.application.price.OrderPrice;
import com.demo.order.application.price.PriceService;
import com.demo.order.domain.Order;
import com.demo.order.infrastructure.OrderRepository;
import com.demo.order.web.OrderRequestParams;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryOrderService {
    private final OrderRepository orderRepository;
    private final PriceService priceService;

    public Page<RichOrder> findAll(OrderRequestParams options) {
        return orderRepository.findAllPage(getPageable(options))
                              .map(this::toRichOrder);
    }

    private Pageable getPageable(OrderRequestParams options) {
        return PageRequest.of(
                options.getPage().get() - 1,
                options.getPageSize().get(),
                Sort.Direction.ASC,
                OrderRequestParams.OrderColumn
        );
    }

    public Optional<RichOrder> findById(Long id) {
        return orderRepository.findById(id)
                              .map(this::toRichOrder);
    }

    private RichOrder toRichOrder(Order order) {
        OrderPrice orderPrice = priceService.calculatePrice(order);
        return new RichOrder(
                order.getId(),
                order.getStatus(),
                order.getItems(),
                order.getRecipient(),
                order.getCreatedAt(),
                orderPrice,
                orderPrice.finalPrice()
        );
    }
}
