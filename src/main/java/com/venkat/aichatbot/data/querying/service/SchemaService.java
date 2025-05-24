package com.venkat.aichatbot.data.querying.service;

import com.venkat.aichatbot.data.querying.Repository.SchemaRepository;
import com.venkat.aichatbot.data.querying.dto.ColumnMetadataDTO;
import com.venkat.aichatbot.data.querying.dto.SchemaDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchemaService {
    private final SchemaRepository schemaRepository;

    public List<SchemaDTO> getSchemaDetails() {
        List<SchemaDTO> schemaList = new ArrayList<>();

        List<String> tableNames = schemaRepository.getAllTableNames();
        for (String table : tableNames) {
            List<Object[]> columns = schemaRepository.getColumnsForTable(table);
            List<ColumnMetadataDTO> columnList = new ArrayList<>();

            for (Object[] col : columns) {
                columnList.add(new ColumnMetadataDTO(
                        String.valueOf(col[0]), // column_name
                        String.valueOf(col[1])  // data_type
                ));
            }

            schemaList.add(new SchemaDTO(table, columnList));
        }

        return schemaList;
    }

    public List<String> getAllTableNames() {
        return schemaRepository.getAllTableNames();
    }

    public List<ColumnMetadataDTO> getColumnsForTable(String tableName) {
        List<Object[]> results = schemaRepository.getColumnsForTable(tableName);
        List<ColumnMetadataDTO> columnList = new ArrayList<>();

        for (Object[] row : results) {
            columnList.add(new ColumnMetadataDTO(
                    String.valueOf(row[0]), // column_name
                    String.valueOf(row[1])  // data_type
            ));
        }

        return columnList;
    }
    public String getSchemaDetailsAsString() {
        List<SchemaDTO> schema = getSchemaDetails(); // Existing method
        StringBuilder builder = new StringBuilder();
        for (SchemaDTO table : schema) {
            builder.append("Table: ").append(table.getTableName()).append("\n");
            for (ColumnMetadataDTO column : table.getColumns()) {
                builder.append("  - ").append(column.getColumnName())
                        .append(" (").append(column.getDataType()).append(")\n");
            }
        }
        return builder.toString();
    }





}
