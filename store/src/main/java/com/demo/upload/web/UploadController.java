package com.demo.upload.web;

import com.demo.upload.application.UploadResponse;
import com.demo.upload.application.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/uploads")
@AllArgsConstructor
@Tag(name = "Uploads", description = "Upload covers API")
class UploadController {
    private final UploadService upload;

    @Operation(
            summary = "Get upload",
            description = "Get upload by id"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "get uploaded file",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
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
    public ResponseEntity<UploadResponse> getUpload(@PathVariable Long id) {
        return upload.getById(id)
                     .map(file -> {
                         UploadResponse response = new UploadResponse(
                                 file.getId(),
                                 file.getContentType(),
                                 file.getFilename(),
                                 file.getCreatedAt()
                         );
                         return ResponseEntity.ok(response);
                     })
                     .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get upload",
            description = "Get upload by id"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "get uploaded file",
                    content = @Content(
                            mediaType = "application/octet-stream"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
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
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getUploadFile(@PathVariable Long id) {
        return upload.getById(id)
                     .map(file -> {
                         String contentDisposition = "attachment; filename=\"" + file.getFilename() + "\"";
                         Resource resource = new ByteArrayResource(file.getFile());
                         return ResponseEntity
                                 .ok()
                                 .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                                 .contentType(MediaType.parseMediaType(file.getContentType()))
                                 .body(resource);
                     })
                     .orElse(ResponseEntity.notFound().build());
    }
}
