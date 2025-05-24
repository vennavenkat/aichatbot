package com.venkat.aichatbot.data.querying.controller;

import com.venkat.aichatbot.data.querying.service.DataService;
import com.venkat.aichatbot.data.querying.service.FileDataService;
import com.venkat.aichatbot.data.querying.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;
    private final FileDataService fileDataService;
    private final FileUploadService fileUploadService;

    @GetMapping("/data/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Map<String, Object>> rows = dataService.getFirstNRows(tableName, limit);
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/filedata/{fileName}")
    public ResponseEntity<List<Map<String, String>>> getSampleRowsFromFile(
            @PathVariable String fileName,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(fileDataService.getSampleRows(fileName, limit));
    }

    @PostMapping("/files/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            fileUploadService.storeFile(file);
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/files/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource fileResource = fileUploadService.loadFileAsResource(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileResource);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(null);
        }
    }






}
