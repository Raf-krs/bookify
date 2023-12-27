package com.demo.order.domain;

import com.demo.order.application.exceptions.InvalidStatusStateChangeException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum OrderStatus {
    NEW {
        @Override
        public UpdateStatusResult updateStatus(OrderStatus status) {
            return switch(status) {
                case PAID -> UpdateStatusResult.ok(PAID);
                case CANCELLED -> UpdateStatusResult.revoked(CANCELLED);
                case ABANDONED -> UpdateStatusResult.revoked(ABANDONED);
                default -> super.updateStatus(status);
            };
        }
    },
    PAID {
        @Override
        public UpdateStatusResult updateStatus(OrderStatus status) {
            if (status == SHIPPED) {
                return UpdateStatusResult.ok(SHIPPED);
            }
            return super.updateStatus(status);
        }
    },
    CANCELLED,
    ABANDONED,
    SHIPPED;

    public static Optional<OrderStatus> parseString(String value) {
        return Arrays.stream(values())
                     .filter(it -> StringUtils.equalsIgnoreCase(it.name(), value))
                     .findFirst();
    }

    public UpdateStatusResult updateStatus(OrderStatus status) {
        throw new InvalidStatusStateChangeException(this.name(), status.name());
    }
}
