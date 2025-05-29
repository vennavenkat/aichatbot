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

    @Column(length = 1000)
    private String comments;

    @Column(length = 10000)
    private String question;

    @Column(length = 10000)
    private String generatedSql;

    @Column(length = 10000)
    private String responseSummary;

    private LocalDateTime timestamp;
}
