package com.demo.shared.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class Problem {

    public static ProblemDetail create(HttpStatus status, String title, String message) {
        var problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(message);

        return problem;
    }
}
