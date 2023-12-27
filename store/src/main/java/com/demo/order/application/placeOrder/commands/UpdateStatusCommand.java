package com.demo.order.application.placeOrder.commands;

import com.demo.order.domain.OrderStatus;
import org.springframework.security.core.userdetails.UserDetails;

public record UpdateStatusCommand(Long orderId, OrderStatus status, UserDetails user) {
}
