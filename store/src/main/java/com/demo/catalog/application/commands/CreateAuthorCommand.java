package com.demo.catalog.application.commands;

import jakarta.validation.constraints.NotBlank;

public record CreateAuthorCommand(
        @NotBlank(message = "Name is required")
        String name
) {
}
