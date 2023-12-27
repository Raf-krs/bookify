package com.demo.catalog.application.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class RestBookCommand {
    @NotBlank(message = "Please provide a title")
    private String title;

    @NotEmpty(message = "Please provide at least one author")
    private Set<Long> authors;

    private int year;

    @Min(value = 0, message = "Available copies must be greater than 0")
    private long available;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    public CreateBookCommand toCreateCommand() {
        return new CreateBookCommand(title, authors, year, price, available);
    }

    public UpdateBookCommand toUpdateCommand(Long id) {
        return new UpdateBookCommand(id, title, authors, year, price);
    }
}
