package com.venkat.aichatbot.data.querying.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;

@Service
public class FileUploadService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String UPLOAD_DIR = "/Users/venkatkumarvenna/Downloads"; // Or use application.properties

    public void storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.endsWith(".csv") || fileName.endsWith(".xlsx"))) {
            throw new RuntimeException("Only CSV and Excel files are supported");
        }

        try {
            Path targetPath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + fileName, e);
        }
    }



    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file: " + fileName, e);
        }
    }
    public List<Map<String, Object>> parseCsv(Path filePath) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String[] headers = reader.readLine().split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                records.add(row);
            }
        }
        return records;
    }

    public List<Map<String, Object>> parseExcel(Path filePath) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();
        try (InputStream is = Files.newInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                Map<String, Object> record = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    record.put(headers.get(j), cell.toString());
                }
                records.add(record);
            }
        }
        return records;
    }

    public String describeAvailableFiles() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            StringBuilder summary = new StringBuilder("Available files and their columns:\n");

            Files.list(uploadPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".csv") || path.toString().endsWith(".xlsx"))
                    .forEach(file -> {
                        try {
                            summary.append("• ").append(file.getFileName()).append(":\n");

                            List<String> columns = extractColumnHeaders(file);
                            for (String col : columns) {
                                summary.append("    - ").append(col).append("\n");
                            }
                        } catch (Exception e) {
                            summary.append("    - Error reading file: ").append(e.getMessage()).append("\n");
                        }
                    });

            return summary.toString();
        } catch (IOException e) {
            return "Error describing uploaded files: " + e.getMessage();
        }
    }

    private List<String> extractColumnHeaders(Path filePath) throws IOException {
        if (filePath.toString().endsWith(".csv")) {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String headerLine = reader.readLine();
                return Arrays.stream(headerLine.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
            }
        } else if (filePath.toString().endsWith(".xlsx")) {
            try (InputStream is = Files.newInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);
                List<String> headers = new ArrayList<>();

                for (Cell cell : headerRow) {
                    headers.add(cell.toString().trim());
                }
                return headers;
            }
        } else {
            return Collections.singletonList("Unsupported file type");
        }
    }


    public void insertCsvDataToTable(String csvFileName, String tableName) {
        String fullPath = UPLOAD_DIR + "/" + csvFileName;

        // 🔍 Step 1: Check if the file exists
        Path path = Paths.get(fullPath);
        if (!Files.exists(path)) {
            throw new RuntimeException("CSV file not found: " + fullPath);
        }

        // 🧾 Step 2: Prepare COPY SQL
        String copySql = String.format(
                "COPY %s FROM '%s' DELIMITER ',' CSV HEADER;",
                tableName, fullPath.replace("\\", "/")
        );

        try {
            SessionFactoryImplementor sessionFactory = entityManager
                    .getEntityManagerFactory()
                    .unwrap(SessionFactoryImplementor.class);

            Connection connection = sessionFactory
                    .getServiceRegistry()
                    .getService(ConnectionProvider.class)
                    .getConnection();

            // 🚀 Step 3: Execute the COPY command via raw JDBC
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(copySql);
            } finally {
                connection.close();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to insert CSV into table: " + e.getMessage(), e);
        }
    }
    public boolean csvExists(String fileName) {
        Path path = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
        return Files.exists(path);
    }

    public void createTableFromCsv(String fileName, String tableName) throws Exception {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("CSV file not found: " + filePath);
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String headerLine = reader.readLine();
            String dataLine = reader.readLine(); // read a sample row

            if (headerLine == null || dataLine == null) {
                throw new RuntimeException("CSV file is empty or missing data");
            }

            String[] headers = headerLine.split(",");
            String[] sampleValues = dataLine.split(",");

            // Infer column types based on sample data
            StringBuilder ddl = new StringBuilder("CREATE TABLE " + tableName + " (\n");
            for (int i = 0; i < headers.length; i++) {
                String column = headers[i].trim();
                String value = (i < sampleValues.length) ? sampleValues[i].trim() : "";

                String type = "VARCHAR(255)";
                if (value.matches("^\\d+$")) {
                    type = "INTEGER";
                } else if (value.matches("^\\d+\\.\\d+$")) {
                    type = "NUMERIC";
                } else if (value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    type = "DATE";
                }

                ddl.append("    ").append(column).append(" ").append(type);
                if (i < headers.length - 1) {
                    ddl.append(",\n");
                }
            }
            ddl.append("\n);");

            // Execute the DDL
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(ddl.toString());
            }
        }
    }
    private Connection getConnection() throws Exception {
        SessionFactoryImplementor sessionFactory = entityManager
                .getEntityManagerFactory()
                .unwrap(SessionFactoryImplementor.class);

        return sessionFactory
                .getServiceRegistry()
                .getService(ConnectionProvider.class)
                .getConnection();
    }

    public void createTableFromExcel(String fileName, String tableName) throws Exception {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);
        try (InputStream inputStream = Files.newInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            StringBuilder ddl = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String columnName = cell.getStringCellValue().trim().replaceAll("[^a-zA-Z0-9_]", "_");
                ddl.append(columnName).append(" TEXT");
                if (i < headerRow.getLastCellNum() - 1) ddl.append(", ");
            }
            ddl.append(");");

            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(ddl.toString());
            }
        }
    }

    public void insertExcelDataToTable(String fileName, String tableName) throws Exception {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);
        try (InputStream inputStream = Files.newInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getLastCellNum();

            String[] columns = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                columns[i] = headerRow.getCell(i).getStringCellValue().trim().replaceAll("[^a-zA-Z0-9_]", "_");
            }

            String placeholders = String.join(",", Collections.nCopies(colCount, "?"));
            String columnNames = String.join(",", columns);
            String insertSQL = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";

            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    for (int j = 0; j < colCount; j++) {
                        Cell cell = row.getCell(j);
                        ps.setString(j + 1, cell != null ? cell.toString() : null);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    public boolean excelExists(String fileName) {
        if (!fileName.endsWith(".xlsx")) return false;
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
        return Files.exists(filePath) && Files.isReadable(filePath);
    }






}
