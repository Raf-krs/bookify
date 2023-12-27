package com.demo.upload.application;

import java.time.LocalDateTime;

public record UploadResponse(Long id, String contentType, String filename, LocalDateTime createdAt) { }
