package com.demo.catalog.infrastructure;

import com.demo.catalog.application.responses.CatalogDto;
import com.demo.catalog.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query(
            countQuery = """
                    SELECT count(id)
                    FROM books b
                    """,
            value = """
                    SELECT b.id, b.title, b.available, b.price, b.year, b.cover_id as coverId, array_agg(a.name) authors
                    FROM books b
                    INNER JOIN books_authors ba ON b.id = ba.books_id
                    LEFT JOIN authors a ON ba.authors_id = a.id
                    GROUP BY b.id
                    """,
            nativeQuery = true
    )
    Page<CatalogDto> findAllPage(Pageable pageable);

    @Query(
            countQuery = """
                    SELECT count(b.id)
                    FROM books b
                    WHERE lower(b.title) LIKE lower(concat('%', :title, '%'))
                    """,
            value = """
                    SELECT b.id, b.title, b.available, b.price, b.year, b.cover_id as coverId, array_agg(a.name) authors
                    FROM books b
                    INNER JOIN books_authors ba ON b.id = ba.books_id
                    LEFT JOIN authors a ON ba.authors_id = a.id
                    WHERE lower(b.title) LIKE lower(concat('%', :title, '%'))
                    GROUP BY b.id
                    """,
            nativeQuery = true
    )
    Page<CatalogDto> findByTitleStartsWithIgnoreCase(Pageable pageable, String title);

    @Query(
            countQuery = """
                    SELECT count(b.id)
                    FROM books b
                    LEFT JOIN books_authors ba ON b.id = ba.books_id
                    LEFT JOIN authors a ON ba.authors_id = a.id
                    WHERE lower(a.name) LIKE lower(concat('%', :author, '%'))
                    """,
            value = """
                    SELECT b.id, b.title, b.available, b.price, b.year, b.cover_id as coverId, array_agg(a.name) authors
                    FROM books b
                    INNER JOIN books_authors ba ON b.id = ba.books_id
                    LEFT JOIN authors a ON ba.authors_id = a.id
                    WHERE lower(a.name) LIKE lower(concat('%', :author, '%'))
                    GROUP BY b.id
                    """,
            nativeQuery = true
    )
    Page<CatalogDto> findByAuthor(Pageable pageable, @Param("author") String author);

    @Query(
            countQuery = """
                    SELECT count(b.id)
                    FROM books b
                    INNER JOIN books_authors ba ON b.id = ba.books_id
                    LEFT JOIN authors a ON ba.authors_id = a.id
                    WHERE lower(a.name) LIKE lower(concat('%', :author, '%'))
                        AND lower(b.title) LIKE lower(concat('%', :title, '%'))
                    """,
            value = """
                    SELECT b.id, b.title, b.available, b.price, b.year, b.cover_id as coverId, array_agg(a.name) authors
                    FROM books b
                    INNER JOIN books_authors ba ON b.id = ba.books_id
                    LEFT JOIN authors a ON ba.authors_id = a.id
                    WHERE lower(a.name) LIKE lower(concat('%', :author, '%'))
                        AND lower(b.title) LIKE lower(concat('%', :title, '%'))
                    GROUP BY b.id
                    """,
            nativeQuery = true
    )
    Page<CatalogDto> findByTitleAndAuthor(Pageable pageable, @Param("title") String title, @Param("author") String author);
}
