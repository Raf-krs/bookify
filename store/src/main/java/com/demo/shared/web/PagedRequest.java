package com.demo.shared.web;

import lombok.Getter;

import java.util.Optional;

@Getter
public class PagedRequest {

    private final Optional<Integer> page;
    private final Optional<Integer> pageSize;

    public PagedRequest(Optional<Integer> page, Optional<Integer> pageSize){
        this.page = page.isEmpty() || page.get() < 0 ? Optional.of(1) : page;
        this.pageSize = pageSize.isEmpty() || pageSize.get() < 1 ? Optional.of(10) : pageSize;
    }
}
