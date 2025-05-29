package com.venkat.aichatbot.data.querying.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;      // e.g., sample_sales_data.csv
    private String tableName;
    @Column(length = 2000)// e.g., sample_sales_data
    private String columns;       // Comma-separated headers like "Region,Total,Category"
    private LocalDateTime uploadedAt;
}
