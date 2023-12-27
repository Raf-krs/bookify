package com.demo.upload.infrastructure;

import com.demo.upload.domain.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadRepository extends JpaRepository<Upload, Long> { }
