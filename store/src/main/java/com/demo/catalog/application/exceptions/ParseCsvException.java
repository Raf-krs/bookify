package com.demo.catalog.application.exceptions;

public class ParseCsvException extends RuntimeException {
    public ParseCsvException() {
        super("Failed to parse CSV file");
    }
}
