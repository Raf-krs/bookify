package com.demo.users.application.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordValidatorTests {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    void isValidShouldFailWhenPasswordDoesNotHaveDigits() {
        // Arrange & Act
        boolean isValid = passwordValidator.isValid("Password@#", null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isValidShouldFailWhenPasswordDoesNotHaveUppercaseLetters() {
        // Arrange & Act
        boolean isValid = passwordValidator.isValid("password@#", null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isValidShouldFailWhenPasswordDoesNotHaveSpecialCharacters() {
        // Arrange & Act
        boolean isValid = passwordValidator.isValid("Password1", null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isValidShouldFailWhenPasswordIsTooShort() {
        // Arrange & Act
        boolean isValid = passwordValidator.isValid("Pass1#", null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isValidShouldPassWhenPasswordIsCorrect() {
        // Arrange & Act
        boolean isValid = passwordValidator.isValid("Password123!", null);

        // Assert
        assertThat(isValid).isTrue();
    }
}
