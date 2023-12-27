package com.demo.users.application.commands;

import com.demo.users.application.validators.Password;

public record ChangePasswordCommand (
        @Password
        String password
) { }
