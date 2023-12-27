package utils;

import com.demo.shared.messaging.RabbitMqConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

public class BaseContainerTests {
    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");
    @Container
    protected static RabbitMQContainer rabbitMq = new RabbitMQContainer("rabbitmq:3.12.10-management-alpine")
            .withQueue(RabbitMqConfig.QUEUE_NAME);
}
