package com.demo.upload.application;

public record SaveUploadCommand(
        String filename,
        byte[] file,
        String contentType
) { }