package com.demo.shared.messaging;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueConfig {

    private final AmqpAdmin amqpAdmin;

    @Value("${rabbitmq.queue.notification}")
    private String notificationQueue;

    @PostConstruct
    public void createQueues() {
        var info = amqpAdmin.getQueueInfo(notificationQueue);
        if (info != null) {
            return;
        }
        amqpAdmin.declareQueue(new Queue(notificationQueue, true));
    }
}