package com.demo.catalog.web;

import com.demo.catalog.domain.Author;
import com.demo.catalog.domain.Book;
import com.demo.catalog.infrastructure.BookRepository;
import com.demo.security.JwtService;
import com.demo.users.domain.Role;
import com.demo.users.domain.User;
import com.demo.users.infrastructure.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import utils.BaseContainerTests;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.expiration=300000",
                "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
        }
)
@Testcontainers
public class CatalogControllerTests extends BaseContainerTests {

    @LocalServerPort
    private int port;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private JwtService jwtService;

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
        bookRepository.deleteAll();
    }

    @Test
    void addCoverShouldReturnAcceptedWhenPassImageFile() {
        var admin = createAdmin();
        var token = jwtService.generateToken(admin);
        var author = new Author("Craig Walls");
        var book = new Book("Spring Boot in action", 2021, new BigDecimal("10.00"), 1L);
        book.addAuthor(author);
        bookRepository.saveAndFlush(book);
        var file = getClass().getClassLoader().getResourceAsStream("static/tests/cover.jpg");

        given()
                .header("Authorization", "Bearer " + token)
                .multiPart("file", "cover.jpg", file, "image/jpeg")
        .when()
                .put("/books/{id}/cover", book.getId())
        .then()
                .log().body()
                .statusCode(HttpStatus.ACCEPTED.value());
    }

    // INFO -> Rest of the tests are omitted

    private User createAdmin() {
        var admin = User.builder()
                        .email("admin@mail.com")
                        .password(passwordEncoder.encode("secret123$"))
                        .role(Role.ADMIN)
                        .build();
        return userRepository.save(admin);
    }
}
