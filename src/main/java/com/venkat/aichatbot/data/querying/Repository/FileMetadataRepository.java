package com.venkat.aichatbot.data.querying.Repository;

import com.venkat.aichatbot.data.querying.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByTableName(String tableName);
}

