package com.venkat.aichatbot.data.querying.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnMetadataDTO {

    private String columnName;

    private String dataType;
}
