package com.demo.catalog.application.commands;

public record UpdateBookCoverCommand(
        Long id,
        byte[] file,
        String contentType,
        String filename
) {
}
