package com.venkat.aichatbot.data.querying.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10000)
    private String question;

    @Column(length = 10000)
    private String generatedSql;

    @Column(length = 10000)
    private String response;

    private LocalDateTime timestamp;
}
