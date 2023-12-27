package com.demo.order.web;

import com.demo.shared.web.PagedRequest;
import lombok.Getter;

import java.util.Optional;

@Getter
public class OrderRequestParams extends PagedRequest {
    private final Optional<String> status;

    public static String OrderColumn = "id";

    public OrderRequestParams(Optional<String> status, Optional<Integer> page, Optional<Integer> pageSize){
        super(page, pageSize);
        this.status = status;
    }
}
