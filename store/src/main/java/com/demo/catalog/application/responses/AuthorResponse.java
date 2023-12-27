package com.demo.catalog.application.responses;

import com.demo.catalog.domain.Author;
import com.demo.shared.Either;

public class AuthorResponse extends Either<String, Author> {
    public AuthorResponse(boolean success, String left, Author right) {
        super(success, left, right);
    }

    public static AuthorResponse success(Author right) {
        return new AuthorResponse(true, null, right);
    }

    public static AuthorResponse failure(String left) {
        return new AuthorResponse(false, left, null);
    }
}
