package com.demo.users.web;

import com.demo.shared.JsonConverter;
import com.demo.users.application.commands.LoginCommand;
import com.demo.users.application.commands.RegisterCommand;
import com.demo.users.application.responses.UserRegistrationPayload;
import com.demo.users.domain.Role;
import com.demo.users.domain.User;
import com.demo.users.infrastructure.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import utils.BaseContainerTests;

import static io.restassured.RestAssured.given;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.expiration=300000",
                "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
        }
)
@Testcontainers
public class UserControllerTests extends BaseContainerTests {

    @LocalServerPort
    private int port;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    protected static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbitMq::getHost);
        registry.add("spring.rabbitmq.port", rabbitMq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMq::getAdminPassword);
        registry.add("spring.rabbitmq.virtual-host", () -> "/");
        registry.add("rabbitmq.queue.notification", () -> "notification-queue");

        registry.add("springdoc.swagger-ui.servers.dev.url", () -> "http://localhost:1234");
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void registerShouldReturn201WhenRegisterCommandIsCorrect() throws JsonProcessingException {
        var command = new RegisterCommand("test@mail.com", "Password123!");

        given()
                .contentType("application/json")
                .body(JsonConverter.toJson(command))
        .when()
                .post("/users/register")
        .then()
                .log().body()
                .statusCode(HttpStatus.ACCEPTED.value());
    }

    @Test
    void registerShouldReturn400WhenRegisterCommandHasEmptyEmail() throws JsonProcessingException {
        var command = new RegisterCommand("", "Password123!");

        given()
                .contentType("application/json")
                .body(JsonConverter.toJson(command))
        .when()
                .post("/users/register")
        .then()
                .log().body()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void loginShouldReturn200WhenLoginCommandIsCorrect() throws JsonProcessingException {
        // Arrange
        var email = "test@mail.com";
        var password = "Password123!";
        var encodedPassword = passwordEncoder.encode(password);
        var user = new User(email, encodedPassword, Role.USER);
        userRepository.save(user);
        var command = new LoginCommand(email, password);

        // Act & Assert
        given()
                .contentType("application/json")
                .body(JsonConverter.toJson(command))
        .when()
                .post("/users/login")
        .then()
                .log().body()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", is(notNullValue()));
    }

    @Test
    void loginShouldReturn401WhenLoginCommandHasWrongPassword() throws JsonProcessingException {
        // Arrange
        var email = "test@mail.com";
        var password = passwordEncoder.encode("Password123!");
        var user = new User(email, password, Role.USER);
        userRepository.save(user);
        var command = new LoginCommand(email, "Password123@");

        // Act & Assert
        given()
                .contentType("application/json")
                .body(JsonConverter.toJson(command))
        .when()
                .post("/users/login")
        .then()
                .log().body()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
