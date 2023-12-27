package com.demo.users.application.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginCommand (
    @Email(message = "Please provide a valid email address")
    String email,

    @NotBlank(message = "Please provide a password")
    String password
) { }
