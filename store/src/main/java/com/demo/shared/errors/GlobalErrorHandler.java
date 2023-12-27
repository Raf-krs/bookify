package com.demo.shared.errors;

import com.demo.catalog.application.exceptions.AuthorNotFoundException;
import com.demo.catalog.application.exceptions.ParseCsvException;
import com.demo.order.application.exceptions.InvalidStatusStateChangeException;
import com.demo.order.application.exceptions.OutOfStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail resourceNotFoundException(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
                       .getFieldErrors()
                       .stream().map(x -> x.getField() + " - " + x.getDefaultMessage())
                       .toList();
        var problemDetail = Problem.create(HttpStatus.BAD_REQUEST, "Bad Request", "Validation Error");
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail resourceNotFoundException(AuthenticationException ex) {
        return Problem.create(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(AuthorNotFoundException.class)
    public ProblemDetail resourceNotFoundException(AuthorNotFoundException ex) {
        var problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problemDetail.setDetail(ex.getMessage());
        problemDetail.setTitle("Author not found");

        return problemDetail;
    }

    @ExceptionHandler(OutOfStockException.class)
    public ProblemDetail resourceNotFoundException(OutOfStockException ex) {
        return Problem.create(HttpStatus.BAD_REQUEST, "Out of stock", ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusStateChangeException.class)
    public ProblemDetail resourceNotFoundException(InvalidStatusStateChangeException ex) {
        return Problem.create(HttpStatus.CONFLICT, "Invalid status state change", ex.getMessage());
    }

    @ExceptionHandler(ParseCsvException.class)
    public ProblemDetail resourceNotFoundException(ParseCsvException ex) {
        return Problem.create(HttpStatus.BAD_REQUEST, "Invalid CSV file", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail resourceNotFoundException(Exception ex) {
        var problemDetail = Problem.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Server Error",
                "Something goes wrong, please try again later."
        );
        problemDetail.setProperty("errors", ex.getMessage());

        return problemDetail;
    }
}
