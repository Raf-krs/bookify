package com.demo.upload.application;

import com.demo.upload.domain.Upload;
import com.demo.upload.infrastructure.UploadRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UploadService {
    private final UploadRepository repository;

    public Upload save(SaveUploadCommand command) {
        Upload upload = new Upload(
                command.filename(),
                command.contentType(),
                command.file()
        );
        repository.save(upload);
        log.info("Upload saved: " + upload.getFilename() + " with id: " + upload.getId());
        return upload;
    }

    public Optional<Upload> getById(Long id) {
        return repository.findById(id);
    }

    public void removeById(Long id) {
        repository.deleteById(id);
    }
}
