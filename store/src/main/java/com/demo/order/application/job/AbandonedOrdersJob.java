package com.demo.order.application.job;

import com.demo.order.application.OrdersProperties;
import com.demo.order.application.placeOrder.commands.UpdateStatusCommand;
import com.demo.order.application.services.ManipulateOrderService;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderStatus;
import com.demo.order.infrastructure.OrderRepository;
import com.demo.shared.clock.Clock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class AbandonedOrdersJob {
    private final OrderRepository orderRepository;
    private final ManipulateOrderService orderService;
    private final OrdersProperties properties;
    private final UserDetails systemUser;
    private final Clock clock;

    @Transactional
    @Scheduled(cron = "${app.orders.abandon-cron}")
    public void run() {
        Duration paymentPeriod = properties.paymentPeriod();
        LocalDateTime olderThan = clock.now().minus(paymentPeriod);
        List<Order> orders = orderRepository.findByStatusAndCreatedAtLessThanEqual(OrderStatus.NEW, olderThan);
        log.info("Found orders to be abandoned: " + orders.size());
        orders.forEach(order -> {
            UpdateStatusCommand command = new UpdateStatusCommand(order.getId(), OrderStatus.ABANDONED, systemUser);
            orderService.updateOrderStatus(command);
        });
    }
}
