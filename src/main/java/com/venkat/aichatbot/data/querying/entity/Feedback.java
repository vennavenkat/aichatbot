package com.venkat.aichatbot.data.querying.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean thumbsUp;
    private String comments;
    private String question;
    private String generatedSql;
    private String responseSummary;

    private LocalDateTime timestamp = LocalDateTime.now();
}
