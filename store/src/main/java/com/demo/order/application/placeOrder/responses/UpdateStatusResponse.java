package com.demo.order.application.placeOrder.responses;

import com.demo.order.application.placeOrder.Error;
import com.demo.order.domain.OrderStatus;
import com.demo.shared.Either;

public class UpdateStatusResponse extends Either<Error, OrderStatus> {
    public UpdateStatusResponse(boolean success, Error left, OrderStatus right) {
        super(success, left, right);
    }

    public static UpdateStatusResponse success(OrderStatus status) {
        return new UpdateStatusResponse(true, null, status);
    }

    public static UpdateStatusResponse failure(Error error) {
        return new UpdateStatusResponse(false, error, null);
    }
}
