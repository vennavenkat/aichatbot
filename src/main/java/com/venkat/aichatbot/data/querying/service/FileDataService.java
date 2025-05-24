package com.venkat.aichatbot.data.querying.service;

import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileDataService {

    private static final String UPLOAD_DIR = "/Users/venkatkumarvenna/Downloads"; // or externalize

    public List<Map<String, String>> getSampleRows(String fileName, int limit) {
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + fileName);
        }

        if (fileName.endsWith(".csv")) {
            return readCsvRows(filePath, limit);
        } else if (fileName.endsWith(".xlsx")) {
            return readExcelRows(filePath, limit);
        } else {
            throw new RuntimeException("Unsupported file type: " + fileName);
        }
    }

    private List<Map<String, String>> readCsvRows(Path filePath, int limit) {
        List<Map<String, String>> data = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            String[] headers = reader.readNext();
            String[] row;
            int count = 0;
            while ((row = reader.readNext()) != null && count++ < limit) {
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    rowMap.put(headers[i], i < row.length ? row[i] : "");
                }
                data.add(rowMap);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV rows", e);
        }
        return data;
    }

    private List<Map<String, String>> readExcelRows(Path filePath, int limit) {
        List<Map<String, String>> data = new ArrayList<>();
        try (InputStream is = new FileInputStream(filePath.toFile());
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();

            headerRow.forEach(cell -> headers.add(cell.toString()));

            for (int i = 1; i <= limit && i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String value = row.getCell(j) != null ? row.getCell(j).toString() : "";
                    rowMap.put(headers.get(j), value);
                }
                data.add(rowMap);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel rows", e);
        }
        return data;
    }
}
