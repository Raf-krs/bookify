package com.demo.users.application;

import com.demo.security.JwtService;
import com.demo.users.domain.Role;
import com.demo.users.domain.User;
import com.demo.users.infrastructure.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTests {

    private UserRepository userRepository;
    private PasswordEncoder encoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        var rabbitTemplate = mock(RabbitTemplate.class);
        userService = new UserService(userRepository, encoder, authenticationManager, jwtService, rabbitTemplate);
    }

    @Test
    public void registerShouldFailWhenEmailIsAlreadyTaken() throws JsonProcessingException {
        // Arrange
        var email = "test@mail.com";
        var password = "Password1#";
        var user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        var response = userService.register(email, password);

        // Assert
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    public void registerShouldPassWhenEmailIsNotTaken() throws JsonProcessingException {
        // Arrange
        var email = "test@mail.com";
        var password = "Password1#";
        var user = new User(email, password, Role.USER);
        user.setId(1L);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(encoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        var response = userService.register(email, password);

        // Assert
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void loginShouldFailWhenEmailIsNotRegistered() {
        // Arrange
        var email = "";
        var password = "Password1#";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        var response = userService.login(email, password);

        // Assert
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    public void loginShouldFailWhenPasswordIsIncorrect() {
        // Arrange
        var email = "";
        var password = "Password1#";
        var user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password)))
                .thenThrow(new AuthenticationException(""){});

        // Act & Assert
        assertThatThrownBy(() -> userService.login(email, password))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void loginShouldPassWhenCredentialsAreCorrect() {
        // Arrange
        var email = "";
        var password = "Password1#";
        var user = new User();
        var token = "token";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(null)).thenReturn(null);
        when(jwtService.generateToken(user)).thenReturn(token);

        // Act
        var response = userService.login(email, password);

        // Assert
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void changePasswordShouldFailWhenEmailIsNotRegistered() {
        // Arrange
        var email = "";
        var password = "Password1#";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        var response = userService.changePassword(email, password);

        // Assert
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    public void changePasswordShouldPassWhenEmailIsRegistered() {
        // Arrange
        var email = "";
        var password = "Password1#";
        var user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        // Act
        var response = userService.changePassword(email, password);

        // Assert
        assertThat(response.isSuccess()).isTrue();
    }
}
