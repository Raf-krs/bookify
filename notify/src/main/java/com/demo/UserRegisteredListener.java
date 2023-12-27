package com.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRegisteredListener {
    public static final String METHOD_NAME = "onMessageReceived";

    public void onMessageReceived(String message) throws JsonProcessingException {
        var payload = JsonConverter.fromJson(message, UserRegistrationPayload.class);

        log.info("Message received");
        log.info("Email Address: " + payload.email());
        log.info("User ID: " + payload.id());

        // INFO -> Only for demo purposes. Simulate sending email, but in reality, I will send an email
        // This functionality should be in a separate microservice
        log.info("Sending email...");
    }
}
