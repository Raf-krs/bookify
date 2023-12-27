package com.demo.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public class Either<L, R> {
    private final boolean success;
    private final L left;
    private final R right;

    public <T> T handle(Function<R, T> onSuccess, Function<L, T> onFailure) {
        if(success) {
            return onSuccess.apply(right);
        }
        return onFailure.apply(left);
    }
}
