package com.demo.catalog.application;

import com.demo.catalog.domain.Author;
import com.demo.catalog.infrastructure.AuthorRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthorService {
    private final AuthorRepository repository;

    public Page<Author> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
