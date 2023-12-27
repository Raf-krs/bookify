package com.demo.order.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.orders")
public record OrdersProperties (
        Duration paymentPeriod,
        String abandonCron
) { }
