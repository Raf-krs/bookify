package com.demo.order.application.placeOrder.commands;

import com.demo.order.domain.Delivery;
import com.demo.order.domain.Recipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderCommand {
    @Singular
    List<OrderItemCommand> items;
    Recipient recipient;

    @Builder.Default
    Delivery delivery = Delivery.COURIER;
}
