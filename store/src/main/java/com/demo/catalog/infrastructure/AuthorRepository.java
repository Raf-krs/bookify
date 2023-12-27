package com.demo.catalog.infrastructure;

import com.demo.catalog.domain.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Query("""
        SELECT a FROM Author a
        INNER JOIN FETCH a.books b
    """)
    Page<Author> findAllPage(Pageable pageable);

    @Query("""
        SELECT a FROM Author a
        INNER JOIN FETCH a.books b
        WHERE lower(a.name) = lower(:name)
    """)
    Optional<Author> findByNameIgnoreCase(@Param("name") String name);
}
