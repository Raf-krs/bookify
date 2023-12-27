package com.demo.catalog.application.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.Set;

public record CreateBookCommand(

        @NotBlank(message = "Please provide a title")
        String title,

        @NotEmpty(message = "Please provide at least one author")
        Set<Long> authors,

        int year,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @Min(value = 0, message = "Available copies must be greater than 0")
        long available) {
}