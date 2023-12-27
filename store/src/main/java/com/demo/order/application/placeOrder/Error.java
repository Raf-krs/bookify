package com.demo.order.application.placeOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum Error {
    NOT_FOUND(HttpStatus.NOT_FOUND),
    FORBIDDEN(HttpStatus.FORBIDDEN);

    private final HttpStatus status;
}
