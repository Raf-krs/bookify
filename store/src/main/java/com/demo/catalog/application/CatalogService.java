package com.demo.catalog.application;

import com.demo.catalog.application.commands.CreateBookCommand;
import com.demo.catalog.application.commands.UpdateBookCommand;
import com.demo.catalog.application.commands.UpdateBookCoverCommand;
import com.demo.catalog.application.exceptions.AuthorNotFoundException;
import com.demo.catalog.application.exceptions.ParseCsvException;
import com.demo.catalog.application.responses.CatalogDto;
import com.demo.catalog.application.responses.UpdateBookResponse;
import com.demo.catalog.domain.Author;
import com.demo.catalog.domain.Book;
import com.demo.catalog.infrastructure.AuthorRepository;
import com.demo.catalog.infrastructure.BookRepository;
import com.demo.catalog.web.BookRequestParams;
import com.demo.shared.money.Money;
import com.demo.upload.application.SaveUploadCommand;
import com.demo.upload.application.UploadService;
import com.demo.upload.domain.Upload;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CatalogService {

    private final BookRepository repository;
    private final AuthorRepository authorRepository;
    private final RestTemplate restTemplate;
    private final UploadService upload;

    public Page<CatalogDto> findAll(BookRequestParams options) {
        var pageable = getPageable(options);
        if (options.getTitle().isPresent() && options.getAuthor().isPresent()) {
            return findByTitleAndAuthor(pageable, options.getTitle().get(), options.getAuthor().get());
        } else if (options.getTitle().isPresent()) {
            return findByTitle(pageable, options.getTitle().get());
        } else if (options.getAuthor().isPresent()) {
            return findByAuthor(pageable, options.getAuthor().get());
        }
        return repository.findAllPage(pageable);
    }

    private Pageable getPageable(BookRequestParams options) {
        return PageRequest.of(
                options.getPage().get() - 1,
                options.getPageSize().get(),
                Sort.Direction.ASC,
                "id"
        );
    }

    public Optional<Book> findById(Long id) {
        return repository.findById(id);
    }

    public Page<CatalogDto> findByTitle(Pageable pageable, String title) {
        return repository.findByTitleStartsWithIgnoreCase(pageable, title);
    }

    public Page<CatalogDto> findByAuthor(Pageable pageable, String author) {
        return repository.findByAuthor(pageable, author);
    }

    public Page<CatalogDto> findByTitleAndAuthor(Pageable pageable, String title, String author) {
        return repository.findByTitleAndAuthor(pageable, title, author);
    }

    public Book addBook(CreateBookCommand command) {
        Book book = toBook(command);
        return repository.save(book);
    }

    private Book toBook(CreateBookCommand command) {
        Book book = new Book(command.title(), command.year(), command.price(), command.available());
        Set<Author> authors = fetchAuthorsByIds(command.authors());
        updateBooks(book, authors);
        return book;
    }

    private void updateBooks(Book book, Set<Author> authors) {
        book.removeAuthors();
        authors.forEach(book::addAuthor);
    }

    private Set<Author> fetchAuthorsByIds(Set<Long> authors) {
        return authors
                .stream()
                .map(authorId -> authorRepository
                        .findById(authorId)
                        .orElseThrow(() -> new AuthorNotFoundException(authorId))
                )
                .collect(Collectors.toSet());
    }

    @Transactional
    public UpdateBookResponse updateBook(UpdateBookCommand command) {
        return repository
                .findById(command.id())
                .map(book -> {
                    updateFields(command, book);
                    return UpdateBookResponse.SUCCESS;
                })
                .orElseGet(() -> new UpdateBookResponse(false, Collections.singletonList("Book not found with id: " + command.id())));
    }

    private Book updateFields(UpdateBookCommand command, Book book) {
        if (command.title() != null) {
            book.setTitle(command.title());
        }
        if (command.authors() != null && !command.authors().isEmpty()) {
            updateBooks(book, fetchAuthorsByIds(command.authors()));
        }
        if (command.year() > 0) {
            book.setYear(command.year());
        }
        if (command.price() != null) {
            book.setPrice(new Money(command.price()));
        }
        return book;

    }

    public void removeById(Long id) {
        repository.deleteById(id);
    }

    public void updateBookCover(UpdateBookCoverCommand command) {
        repository.findById(command.id())
                  .ifPresent(book -> {
                      Upload savedUpload = upload.save(new SaveUploadCommand(command.filename(),
                                                                             command.file(),
                                                                             command.contentType()));
                      book.setCoverId(savedUpload.getId());
                      repository.save(book);
                  });
    }

    public void removeBookCover(Long id) {
        repository.findById(id)
                  .ifPresent(book -> {
                      if (book.getCoverId() != null) {
                          upload.removeById(book.getCoverId());
                          book.setCoverId(null);
                          repository.save(book);
                      }
                  });
    }

    @Async
    @Transactional
    public void addFromCsv(byte[] csvFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvFile)))) {
            CsvToBean<CsvBook> build = new CsvToBeanBuilder<CsvBook>(reader)
                    .withType(CsvBook.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            build.stream()
                 .filter(csvBook -> StringUtils.isNotBlank(csvBook.title) &&
                                    StringUtils.isNotBlank(csvBook.authors) &&
                                    csvBook.year != null &&
                                    csvBook.amount != null &&
                                    StringUtils.isNotBlank(csvBook.thumbnail))
                 .forEach(this::initBook);
        } catch (Exception exception) {
            throw new ParseCsvException();
        }
    }

    private void initBook(CsvBook csvBook) {
        Set<Long> authors = Arrays
                .stream(csvBook.authors.split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(this::getOrCreateAuthor)
                .map(Author::getId)
                .collect(Collectors.toSet());
        CreateBookCommand command = new CreateBookCommand(
                csvBook.title,
                authors,
                csvBook.year,
                csvBook.amount,
                50L
        );
        Book book = addBook(command);
        updateBookCover(updateBookCoverCommand(book.getId(), csvBook.thumbnail));
    }

    private Author getOrCreateAuthor(String name) {
        return authorRepository
                .findByNameIgnoreCase(name)
                .orElseGet(() -> authorRepository.save(new Author(name)));
    }

    private UpdateBookCoverCommand updateBookCoverCommand(Long bookId, String thumbnailUrl) {
        ResponseEntity<byte[]> response = restTemplate.exchange(thumbnailUrl, HttpMethod.GET, null, byte[].class);
        String contentType = Objects.requireNonNull(response.getHeaders().getContentType()).toString();
        return new UpdateBookCoverCommand(bookId, response.getBody(), contentType, "cover");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CsvBook {
        @CsvBindByName
        private String title;
        @CsvBindByName
        private String authors;
        @CsvBindByName
        private Integer year;
        @CsvBindByName
        private BigDecimal amount;
        @CsvBindByName
        private String thumbnail;
    }
}