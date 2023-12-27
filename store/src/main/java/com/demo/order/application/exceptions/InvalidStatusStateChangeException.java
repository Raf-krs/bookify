package com.demo.order.application.exceptions;

public class InvalidStatusStateChangeException extends RuntimeException {
    public InvalidStatusStateChangeException(String oldStatus, String newStatus) {
        super("Unable to mark " + oldStatus + " order as " + newStatus);
    }
}
