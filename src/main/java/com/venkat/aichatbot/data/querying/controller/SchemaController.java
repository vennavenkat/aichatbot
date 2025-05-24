package com.venkat.aichatbot.data.querying.controller;

import com.venkat.aichatbot.data.querying.dto.ColumnMetadataDTO;
import com.venkat.aichatbot.data.querying.dto.FileSchemaDTO;
import com.venkat.aichatbot.data.querying.dto.SchemaDTO;
import com.venkat.aichatbot.data.querying.service.FileSchemaService;
import com.venkat.aichatbot.data.querying.service.SchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;
    private final FileSchemaService fileSchemaService;

    @GetMapping("/schema")
    public ResponseEntity<List<SchemaDTO>> getSchemaDetails() {
        return ResponseEntity.ok(schemaService.getSchemaDetails());
    }

    @GetMapping("/tables")
    public ResponseEntity<List<String>> getAllTables() {
        List<String> tables = schemaService.getAllTableNames();
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/columns/table/{tableName}")
    public ResponseEntity<List<ColumnMetadataDTO>> getTableColumns(@PathVariable String tableName) {
        List<ColumnMetadataDTO> columns = schemaService.getColumnsForTable(tableName);
        return ResponseEntity.ok(columns);
    }

    @GetMapping("/file/list")
    public ResponseEntity<List<FileSchemaDTO>> listFilesWithSchema() {
        return ResponseEntity.ok(fileSchemaService.listFilesWithSchema());
    }

    @GetMapping("/file/columns/{fileName}")
    public ResponseEntity<List<String>> getFileColumns(@PathVariable String fileName) {
        try {
            List<String> columns = fileSchemaService.getColumnsFromFile(fileName);
            return ResponseEntity.ok(columns);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(List.of("Error: " + e.getMessage()));
        }
    }


}
