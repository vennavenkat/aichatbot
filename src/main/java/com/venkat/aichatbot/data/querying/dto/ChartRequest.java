package com.venkat.aichatbot.data.querying.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartRequest {

    private String chartType; // e.g. "bar", "line", "pie"
    private List<String> labels;
    private List<Number> values;
}
