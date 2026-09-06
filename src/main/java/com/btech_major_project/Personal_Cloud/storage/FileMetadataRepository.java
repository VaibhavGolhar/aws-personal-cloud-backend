package com.btech_major_project.Personal_Cloud.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Page<FileMetadata> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<FileMetadata> findByIdAndUserId(Long id, Long userId);
}
