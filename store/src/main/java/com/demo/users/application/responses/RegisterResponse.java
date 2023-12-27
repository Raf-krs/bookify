package com.demo.users.application.responses;

import com.demo.shared.Either;
import com.demo.users.domain.User;

public class RegisterResponse extends Either<String, User> {

    public RegisterResponse(boolean success, String error, User entity) {
        super(success, error, entity);
    }

    public static RegisterResponse success(User entity) {
        return new RegisterResponse(true, null, entity);
    }

    public static RegisterResponse failure(String error) {
        return new RegisterResponse(false, error, null);
    }
}
