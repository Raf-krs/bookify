package com.demo.users.application.responses;

import com.demo.shared.Either;
import com.demo.users.domain.User;

public class ChangePasswordResponse extends Either<String, User> {
    private ChangePasswordResponse(boolean success, String error, User entity) {
        super(success, error, entity);
    }

    public static ChangePasswordResponse success(User entity) {
        return new ChangePasswordResponse(true, null, entity);
    }

    public static ChangePasswordResponse failure(String error) {
        return new ChangePasswordResponse(false, error, null);
    }
}
