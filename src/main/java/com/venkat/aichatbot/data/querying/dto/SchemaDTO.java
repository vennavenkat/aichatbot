package com.venkat.aichatbot.data.querying.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaDTO {

    private String tableName;

    private List<ColumnMetadataDTO> columns;


}
