package com.demo.users.application.commands;

import com.demo.users.application.validators.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterCommand (
    @NotBlank(message = "Please provide a valid email address")
    @Email(message = "Please provide a valid email address")
    String email,

    @Password
    String password
) {}
