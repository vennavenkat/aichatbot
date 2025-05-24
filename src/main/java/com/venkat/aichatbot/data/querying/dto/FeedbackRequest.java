package com.venkat.aichatbot.data.querying.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {

    private boolean thumbsUp;          // true for 👍, false for 👎
    private String comments;           // optional user message
    private String question;           // the original question (optional context)
    private String generatedSql;       // if any
    private String responseSummary;    // the answer/summary shown to the user
}
