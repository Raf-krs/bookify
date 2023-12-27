package com.demo.catalog.web;

import com.demo.shared.web.PagedRequest;
import lombok.Getter;

import java.util.Optional;

@Getter
public class BookRequestParams extends PagedRequest {
    private final Optional<String> title;
    private final Optional<String> author;

    public BookRequestParams(Optional<String> title, Optional<String> author,
                             Optional<Integer> page, Optional<Integer> pageSize){
        super(page, pageSize);
        this.title = title;
        this.author = author;
    }
}
