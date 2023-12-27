package com.demo.order.application.placeOrder.commands;

import jakarta.validation.constraints.Min;

public record OrderItemCommand(
        Long bookId,

        @Min(value = 1, message = "Quantity must be greater than 0")
        int quantity
) {
}
