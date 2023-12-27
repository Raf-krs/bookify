package com.demo.users.web;

import com.demo.users.application.UserService;
import com.demo.users.application.commands.ChangePasswordCommand;
import com.demo.users.application.commands.LoginCommand;
import com.demo.users.application.commands.RegisterCommand;
import com.demo.users.application.responses.LoginDto;
import com.demo.users.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Users API")
class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register a new user",
            description = "Register a new user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "User registered",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody RegisterCommand command) throws JsonProcessingException {
        return userService
                .register(command.email(), command.password())
                .handle(
                        entity -> ResponseEntity.accepted().build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    @Operation(
            summary = "Login a user",
            description = "Login a user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginCommand command) {
        return userService
                .login(command.email(), command.password())
                .handle(
                        ResponseEntity::ok,
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    @Operation(
            summary = "Change password",
            description = "Change password"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Password changed",
                    content = @Content(mediaType = "application/problem+json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured({"USER", "ADMIN"})
    @PostMapping("/password")
    public ResponseEntity changePassword(@Valid @RequestBody ChangePasswordCommand command, Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        return userService
                .changePassword(user.getEmail(), command.password())
                .handle(
                        entity -> ResponseEntity.accepted().build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }
}
