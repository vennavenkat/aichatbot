package com.venkat.aichatbot.data.querying.service;

import com.opencsv.CSVReader;
import com.venkat.aichatbot.data.querying.dto.FileSchemaDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileSchemaService {

    private static final String UPLOAD_DIR = "/Users/venkatkumarvenna/Downloads"; // Change path if needed

    public List<FileSchemaDTO> listFilesWithSchema() {
        List<FileSchemaDTO> fileSchemas = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(UPLOAD_DIR))) {
            for (Path filePath : stream) {
                String fileName = filePath.getFileName().toString();
                if (fileName.endsWith(".csv")) {
                    fileSchemas.add(new FileSchemaDTO(fileName, extractCsvHeaders(filePath)));
                } else if (fileName.endsWith(".xlsx")) {
                    fileSchemas.add(new FileSchemaDTO(fileName, extractExcelHeaders(filePath)));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded files", e);
        }

        return fileSchemas;
    }

    private List<String> extractCsvHeaders(Path filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            String[] header = reader.readNext();
            return header != null ? Arrays.asList(header) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> extractExcelHeaders(Path filePath) {
        try (InputStream is = new FileInputStream(filePath.toFile());
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            if (row != null) {
                row.forEach(cell -> headers.add(cell.toString()));
            }
            return headers;
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<String> getColumnsFromFile(String fileName) {
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + fileName);
        }

        if (fileName.endsWith(".csv")) {
            return extractCsvHeaders(filePath);
        } else if (fileName.endsWith(".xlsx")) {
            return extractExcelHeaders(filePath);
        } else {
            throw new RuntimeException("Unsupported file type: " + fileName);
        }
    }

}
