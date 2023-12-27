package com.demo.orders.web;

import com.demo.catalog.domain.Book;
import com.demo.catalog.infrastructure.BookRepository;
import com.demo.order.application.placeOrder.commands.OrderItemCommand;
import com.demo.order.application.placeOrder.commands.PlaceOrderCommand;
import com.demo.order.domain.Delivery;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderItem;
import com.demo.order.domain.Recipient;
import com.demo.order.infrastructure.OrderRepository;
import com.demo.order.infrastructure.RecipientRepository;
import com.demo.security.JwtService;
import com.demo.shared.JsonConverter;
import com.demo.shared.clock.Clock;
import com.demo.users.domain.Role;
import com.demo.users.domain.User;
import com.demo.users.infrastructure.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.expiration=300000",
                "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
        }
)
@Testcontainers
public class OrderControllerTests extends BaseContainerTests {

    @LocalServerPort
    private int port;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private Clock clock;
    private final Faker faker = new Faker();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
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
        orderRepository.deleteAll();
        userRepository.deleteAll();
        recipientRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void getOrdersShouldReturn200WithValidPage() {
        // Arrange
        var admin = createAdmin();
        var books = bookRepository.saveAll(generateBooks(5));
        orderRepository.saveAll(generateOrders(15, books));
        var token = jwtService.generateToken(admin);

        // Act & Assert
        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 1)
                .queryParam("pageSize", 10)
        .when()
                .get("/orders")
        .then()
                .log().body()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", is(10));
    }

    @Test
    void postOrderShouldReturnCreatedResponse() throws JsonProcessingException {
        // Arrange
        var admin = createAdmin();
        var books = bookRepository.saveAll(generateBooks(2));
        var token = jwtService.generateToken(admin);
        var recipient = new Recipient();
        recipientRepository.save(recipient);
        var command = new PlaceOrderCommand(List.of(
                new OrderItemCommand(books.get(0).getId(), 1),
                new OrderItemCommand(books.get(1).getId(), 2)
        ), recipient, Delivery.SELF_PICKUP);

        // Act & Assert
        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(JsonConverter.toJson(command))
        .when()
                .post("/orders")
        .then()
                .log().body()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", matchesPattern(".*/orders/[0-9]+"));
    }

    // INFO: Rest of the tests are omitted

    private User createAdmin() {
       var admin = User.builder()
                       .email("admin@mail.com")
                       .password(passwordEncoder.encode("Secret123$"))
                       .role(Role.ADMIN)
                       .createdAt(clock.now())
                       .build();
       return userRepository.save(admin);
    }

    private List<Book> generateBooks(int count) {
        var books = new ArrayList<Book>();
        var titles = generateTitles(30);
        for(int i = 0; i < count; i++) {
            books.add(
                    new Book(
                            titles.get(faker.number().numberBetween(0, titles.size())),
                            faker.number().numberBetween(1700, 2023),
                            new BigDecimal(faker.number().numberBetween(1, 1000)),
                            faker.number().numberBetween(0L, 100L)
                    )
            );
        }

        return books;
    }

    private List<String> generateTitles(int count) {
        var bookTitles = new HashSet<String>();
        for(int i = 0; i < count; i++) {
            bookTitles.add(faker.book().title());
        }

        return bookTitles.stream().toList();
    }

    private List<Order> generateOrders(int count, List<Book> books) {
        var orders = new ArrayList<Order>();
        for(int i = 0; i < count; i++) {
            orders.add(
                    Order.builder()
                         .item(new OrderItem(
                                 books.get(faker.number().numberBetween(0, books.size())), i
                         ))
                         .recipient(new Recipient())
                         .build()
            );
        }

        return orders;
    }
}
