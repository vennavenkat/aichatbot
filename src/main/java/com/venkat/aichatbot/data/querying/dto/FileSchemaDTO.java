package com.venkat.aichatbot.data.querying.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileSchemaDTO {
    private String fileName;
    private List<String> columns;
}
