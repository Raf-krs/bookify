package com.demo.catalog.application.exceptions;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(long id) {
        super("Author with id " + id + " not found");
    }
}
