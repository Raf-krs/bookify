package com.demo.catalog.application.responses;

import lombok.Value;

import java.util.List;

import static java.util.Collections.emptyList;

@Value
public class UpdateBookResponse {
    public static UpdateBookResponse SUCCESS = new UpdateBookResponse(true, emptyList());

    boolean success;
    List<String> errors;
}
