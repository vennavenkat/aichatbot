package com.venkat.aichatbot.data.querying.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatLogDTO {
    private String question;
    private String generatedSql;
    private String response;
    private LocalDateTime timestamp;
}
