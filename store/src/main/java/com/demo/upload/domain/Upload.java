package com.demo.upload.domain;

import com.demo.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploads")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Upload extends BaseEntity {

    private byte[] file;

    @Column(length = 5)
    private String contentType;

    private String filename;

    @CreatedDate
    private LocalDateTime createdAt;

    public Upload(String filename, String contentType, byte[] file) {
        this.filename = filename;
        this.contentType = contentType;
        this.file = file;
    }
}
