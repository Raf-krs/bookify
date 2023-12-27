package com.demo.shared.web;

import lombok.AllArgsConstructor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@AllArgsConstructor
public class CreatedUri {
    private final String path;

    public URI uri() {
        return ServletUriComponentsBuilder.fromCurrentRequestUri().path(path).build().toUri();
    }
}