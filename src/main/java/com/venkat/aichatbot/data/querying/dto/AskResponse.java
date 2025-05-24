package com.venkat.aichatbot.data.querying.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AskResponse {

    private String answer;
    private String generatedSql;
    private List<Map<String, Object>> resultData; // NEW FIELD for SQL result
}
