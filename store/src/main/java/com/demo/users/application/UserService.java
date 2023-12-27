package com.demo.users.application;

import com.demo.security.JwtService;
import com.demo.shared.JsonConverter;
import com.demo.shared.messaging.RabbitMqConfig;
import com.demo.users.application.responses.ChangePasswordResponse;
import com.demo.users.application.responses.LoginDto;
import com.demo.users.application.responses.LoginResponse;
import com.demo.users.application.responses.RegisterResponse;
import com.demo.users.application.responses.UserRegistrationPayload;
import com.demo.users.domain.Role;
import com.demo.users.domain.User;
import com.demo.users.infrastructure.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public RegisterResponse register(String email, String password) throws JsonProcessingException {
        if (userRepository.findByEmail(email).isPresent()) {
            return RegisterResponse.failure("Account already exists");
        }
        var entity = User.builder()
                         .email(email)
                         .password(encoder.encode(password))
                         .role(Role.USER)
                         .build();
        var savedUser = userRepository.save(entity);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.QUEUE_NAME,
                JsonConverter.toJson(new UserRegistrationPayload(savedUser.getId(), savedUser.getEmail()))
        );

        return RegisterResponse.success(savedUser);
    }

    public LoginResponse login(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        var user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            return LoginResponse.failure("User not found");
        }
        var token = jwtService.generateToken(user.get());
        return LoginResponse.success(new LoginDto(token));
    }

    public ChangePasswordResponse changePassword(String email, String password) {
        var user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            return ChangePasswordResponse.failure("User not found");
        }
        user.get().setPassword(encoder.encode(password));
        return ChangePasswordResponse.success(userRepository.save(user.get()));
    }
}
