package com.demo.catalog.application.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;

@Builder
public record UpdateBookCommand(
        long id,

        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Authors are required")
        Set<Long> authors,

        int year,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price) {
}
