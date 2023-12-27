package com.demo.order.application.exceptions;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(long bookId, int quantityRequested, long availableQuantity) {
        super("Too many copies of book " + bookId + " requested: " + quantityRequested + " of "
                      + availableQuantity + " available");
    }
}
