package com.demo.users.application.responses;

import com.demo.shared.Either;

public class LoginResponse extends Either<String, LoginDto> {

    private LoginResponse(boolean success, String error, LoginDto response) {
        super(success, error, response);
    }

    public static LoginResponse success(LoginDto response) {
        return new LoginResponse(true, null, response);
    }

    public static LoginResponse failure(String error) {
        return new LoginResponse(false, error, null);
    }
}
