package com.demo.catalog.web;

import com.demo.catalog.application.CatalogService;
import com.demo.catalog.application.commands.RestBookCommand;
import com.demo.catalog.application.commands.UpdateBookCoverCommand;
import com.demo.catalog.application.responses.CatalogDto;
import com.demo.catalog.domain.Book;
import com.demo.shared.errors.Problem;
import com.demo.shared.web.CreatedUri;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/books")
@RestController
@AllArgsConstructor
@Tag(name = "Books", description = "Books catalog API")
class CatalogController {
    private final CatalogService catalog;

    @Operation(
            summary = "Get all books",
            description = "Get all books with pagination and filtering options"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Get books",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<RestBook> getAll(
            @RequestParam(required = false, name = "title") Optional<String> title,
            @RequestParam(required = false, name = "author") Optional<String> author,
            @RequestParam(required = false, name = "page") Optional<Integer> page,
            @RequestParam(required = false, name = "pageSize") Optional<Integer> pageSize,
            HttpServletRequest request
    ) {
        var options = new BookRequestParams(title, author, page, pageSize);
        return catalog.findAll(options).map(book -> toRestBook(book, request));
    }

    private RestBook toRestBook(CatalogDto book, HttpServletRequest request) {
        String coverUrl = Optional
                .ofNullable(book.getCoverId())
                .map(coverId -> ServletUriComponentsBuilder
                        .fromContextPath(request)
                        .path("/uploads/{id}/file")
                        .build(coverId)
                        .toASCIIString())
                .orElse(null);

        return new RestBook(
                book.getId(),
                book.getTitle(),
                book.getYear(),
                book.getPrice(),
                coverUrl,
                book.getAvailable(),
                toRestAuthors(book.getAuthors())
        );
    }

    private Set<RestAuthor> toRestAuthors(List<String> authors) {
        return authors.stream()
                      .map(RestAuthor::new)
                      .collect(Collectors.toSet());
    }

    @Operation(
            summary = "Get book by id",
            description = "Get book by id"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Get book",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Book.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity getById(@PathVariable Long id) {
        return catalog
                .findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Update book by id",
            description = "Update book by id"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Accepted",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ADMIN")
    @PatchMapping("/{id}")
    public ResponseEntity updateBook(@PathVariable Long id, @Valid @RequestBody RestBookCommand command) {
        var response = catalog.updateBook(command.toUpdateCommand(id));
        if (!response.isSuccess()) {
            String message = String.join(", ", response.getErrors());
            return ResponseEntity.badRequest().body(Problem.create(HttpStatus.BAD_REQUEST, "Bad Request", message));
        }
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Update book by id",
            description = "Update book by id"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Accepted",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ADMIN")
    @PutMapping(value = "/{id}/cover", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity addBookCover(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "file") MultipartFile file
    ) throws IOException {
        if (!isImage(Objects.requireNonNull(file.getContentType()))) {
            return ResponseEntity.badRequest().body("Only images are allowed");
        }

        catalog.updateBookCover(new UpdateBookCoverCommand(
                id,
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename()
        ));
        return ResponseEntity.accepted().build();
    }

    private boolean isImage(String contentType) {
        return contentType.startsWith("image/");
    }

    @Operation(
            summary = "Remove book cover by id",
            description = "Remove book cover by id"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ADMIN")
    @DeleteMapping("/{id}/cover")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookCover(@PathVariable Long id) {
        catalog.removeBookCover(id);
    }

    @Operation(
            summary = "Add book",
            description = "Add book"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ADMIN")
    @PostMapping
    public ResponseEntity addBook(@Valid @RequestBody RestBookCommand command) {
        Book book = catalog.addBook(command.toCreateCommand());
        return ResponseEntity.created(createdBookUri(book)).build();
    }

    @Operation(
            summary = "Import books from csv",
            description = "Import books from csv"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Accepted",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ADMIN")
    @PostMapping(value = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity addFromCsv(@RequestParam("file") MultipartFile csv) throws IOException {
        if(!isCsv(Objects.requireNonNull(csv.getContentType()))) {
            return ResponseEntity.badRequest().body("Only csv are allowed");
        }
        catalog.addFromCsv(csv.getBytes());

        return ResponseEntity.accepted().build();
    }

    private boolean isCsv(String contentType) {
        return contentType.startsWith("text/csv");
    }

    @Operation(
            summary = "Delete book by id",
            description = "Delete book by id"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        catalog.removeById(id);
    }

    private URI createdBookUri(Book book) {
        return new CreatedUri("/" + book.getId().toString()).uri();
    }

    @Value
    class RestBook {
        Long id;
        String title;
        Integer year;
        BigDecimal price;
        String coverUrl;
        Long available;
        Set<RestAuthor> authors;
    }

    @Value
    class RestAuthor {
        String name;
    }
}
