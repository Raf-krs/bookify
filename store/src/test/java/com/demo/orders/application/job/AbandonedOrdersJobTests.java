package com.demo.orders.application.job;

import com.demo.catalog.domain.Author;
import com.demo.catalog.domain.Book;
import com.demo.catalog.infrastructure.BookRepository;
import com.demo.order.application.job.AbandonedOrdersJob;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderItem;
import com.demo.order.domain.OrderStatus;
import com.demo.order.domain.Recipient;
import com.demo.order.infrastructure.OrderRepository;
import com.demo.shared.clock.Clock;
import com.demo.shared.clock.SystemClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import utils.BaseContainerTests;
import utils.FakeClock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.time.Duration.ofHours;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(
        webEnvironment = NONE,
        properties = {
                "app.orders.abandon-cron=*/3 * * * * *",
                "app.orders.payment-period=1H",
                "jwt.expiration=300000",
                "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
        }
)
@Testcontainers
public class AbandonedOrdersJobTests extends BaseContainerTests {

    @SpyBean
    AbandonedOrdersJob abandonedOrdersJob;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    FakeClock fakeClock;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public FakeClock clock() {
            return new FakeClock();
        }
    }

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

        registry.add("springdoc.swagger-ui.servers.dev.url", () -> "http://localhost:1234");
    }

    @AfterEach
    public void tearDown() {
        orderRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void runJobShouldMarkAbandonedOrders() {
        // Arrange
        var book = createBook();
        book.addAuthor(createAuthor());
        bookRepository.saveAndFlush(book);
        var order = createOrder(book, createRecipient());
        orderRepository.saveAndFlush(order);

        // Act
        fakeClock.tick(ofHours(2));
        abandonedOrdersJob.run();

        // Assert
        await().atMost(4, SECONDS)
               .untilAsserted(() -> verify(abandonedOrdersJob, atLeast(1)).run());
        var updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.ABANDONED);
    }
    private Author createAuthor() {
        return new Author("J. R. R. Tolkien");
    }

    private Book createBook() {
        return new Book("The Lord of the Rings", 1954, BigDecimal.valueOf(19.99), 100L);
    }

    private Recipient createRecipient() {
        return Recipient.builder()
                        .name("John Doe")
                        .city("New York")
                        .zipCode("12345")
                        .build();
    }

    private Order createOrder(Book book, Recipient recipient) {
        return Order.builder()
                    .item(new OrderItem(book, 1))
                    .recipient(recipient)
                    .status(OrderStatus.NEW)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();
    }
}
