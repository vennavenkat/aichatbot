package com.venkat.aichatbot.data.querying.service;

import com.venkat.aichatbot.data.querying.Repository.ChatLogRepository;
import com.venkat.aichatbot.data.querying.Repository.FileMetadataRepository;
import com.venkat.aichatbot.data.querying.client.OpenAIClient;
import com.venkat.aichatbot.data.querying.dto.AskResponse;
import com.venkat.aichatbot.data.querying.entity.ChatLog;
import com.venkat.aichatbot.data.querying.entity.FileMetadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import java.sql.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    @PersistenceContext
    private EntityManager entityManager;

    private final OpenAIClient openAIClient;
    private final ChatLogRepository chatLogRepository;
    private final SchemaService schemaService;
    private final DynamicQueryRepository dynamicQueryRepository;
    private final FileUploadService fileUploadService;

    public AskResponse processQuestion(String question) {
        String schemaSummary = schemaService.getSchemaDetailsAsString();
        List<FileMetadata> all = fileUploadService.getAllMetadata(); // Create this method in your service
        StringBuilder schemaPrompt = new StringBuilder("Uploaded files:\n");
        for (FileMetadata meta : all) {
            schemaPrompt.append("- Table: ").append(meta.getTableName())
                    .append(" → Columns: ").append(meta.getColumns())
                    .append("\n");
        }
        String fileMetadata = schemaPrompt.toString();


        String systemPrompt = "You are a data assistant. Here is the schema:\n" + schemaSummary +
                "\nHere is information about uploaded files:\n" + fileMetadata +
                "\nPlease answer the user's question and generate SQL if applicable.";

        String modifiedQuestion = question + ". Also generate the SQL query for this using the schema above. Enclose SQL in <sql> </sql>.";

        String gptResponse = openAIClient.askGPT(systemPrompt, modifiedQuestion);

        String sql = extractSqlFromResponse(gptResponse);
        String explanation = removeSqlFromResponse(gptResponse);

        List<Map<String, Object>> resultSet = new ArrayList<>();
        if (!sql.isBlank()) {
            resultSet = executeDynamicSql(sql);

            // 🔍 Check if it's a SELECT from a table that doesn't exist
            if (resultSet.size() == 1 && resultSet.get(0).containsKey("error")) {
                String errorMessage = String.valueOf(resultSet.get(0).get("error"));
                if (errorMessage.toLowerCase().contains("relation") && errorMessage.toLowerCase().contains("does not exist")) {
                    String tableName = extractTableNameFromSelect(sql); // ⬅️ see helper below

                    // ✅ If CSV exists, try creating and loading data
                    if (resultSet.size() == 1 && resultSet.get(0).containsKey("error")) {
                         errorMessage = String.valueOf(resultSet.get(0).get("error"));
                        if (errorMessage.toLowerCase().contains("relation") && errorMessage.toLowerCase().contains("does not exist")) {
                             tableName = extractTableNameFromSelect(sql);
                            try {
                                tryLoadFileAndInsert(tableName); // 🔄 Support both CSV/XLSX
                                resultSet = executeDynamicSql(sql);
                            } catch (Exception ex) {
                                Map<String, Object> error = new HashMap<>();
                                error.put("error", "Auto-recovery failed: " + ex.getMessage());
                                resultSet.add(error);
                            }
                        }
                    }

                }
            }

            // ✅ Old logic for CREATE TABLE from LLM
            if (sql.toLowerCase().contains("create table")) {
                String createdTable = extractTableNameFromCreateStatement(sql);
                if (fileUploadService.csvExists(createdTable + ".csv")) {
                    try {
                        fileUploadService.insertCsvDataToTable(createdTable + ".csv", createdTable);
                    } catch (Exception e) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("error", "Data insert from CSV failed: " + e.getMessage());
                        resultSet.add(error);
                    }
                }
            }
    }

//        ChatLog log = new ChatLog(null, question, sql, explanation, LocalDateTime.now());
        ChatLog log = new ChatLog(null, question, sql, explanation, LocalDateTime.now(), null);

        chatLogRepository.save(log);

        AskResponse response = new AskResponse();
        response.setAnswer(explanation.trim());
        response.setGeneratedSql(sql.trim());
        response.setResultData(resultSet);

        return response;
    }
    private String extractTableNameFromSelect(String sql) {
        String lower = sql.toLowerCase();
        if (lower.contains("from")) {
            String[] parts = lower.split("from");
            if (parts.length > 1) {
                String[] tokens = parts[1].trim().split("\\s+");
                return tokens[0].replaceAll("[;]", ""); // remove any trailing semicolon
            }
        }
        return "";
    }


    public List<Map<String, Object>> executeDynamicSql(String sql) {
        return dynamicQueryRepository.executeDynamicSql(sql);
    }

    private String extractTableNameFromCreateStatement(String sql) {
        String lower = sql.toLowerCase();
        int start = lower.indexOf("create table") + "create table".length();
        int end = lower.indexOf("(", start);
        String tablePart = sql.substring(start, end).trim();
        return tablePart.contains(" ") ? tablePart.split("\\s+")[0] : tablePart;
    }

    private String extractSqlFromResponse(String gptResponse) {
        if (gptResponse.contains("<sql>")) {
            int start = gptResponse.indexOf("<sql>") + 5;
            int end = gptResponse.indexOf("</sql>");
            if (start < end) {
                return gptResponse.substring(start, end).replaceAll("(?s)`+", "").trim();
            }
        }

        if (gptResponse.contains("```sql")) {
            int start = gptResponse.indexOf("```sql") + 6;
            int end = gptResponse.indexOf("```", start);
            if (start < end) {
                return gptResponse.substring(start, end).replaceAll("(?s)`+", "").trim();
            }
        }

        return gptResponse.replaceAll("(?s)`+", "").trim();
    }

    private String removeSqlFromResponse(String gptResponse) {
        return gptResponse.replaceAll("<sql>.*?</sql>", "").trim();
    }

    public boolean checkDatabase() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkLLM() {
        try {
            String ping = openAIClient.askGPT("You are a health check bot. Say 'pong'.", "ping");
            return ping.toLowerCase().contains("pong");
        } catch (Exception e) {
            return false;
        }
    }

    private void tryLoadFileAndInsert(String tableName) throws Exception {
        if (fileUploadService.csvExists(tableName + ".csv")) {
            fileUploadService.createTableFromCsv(tableName + ".csv", tableName);
            fileUploadService.insertCsvDataToTable(tableName + ".csv", tableName);
        } else if (fileUploadService.excelExists(tableName + ".xlsx")) {
            fileUploadService.createTableFromExcel(tableName + ".xlsx", tableName);
            fileUploadService.insertExcelDataToTable(tableName + ".xlsx", tableName);
        }
    }






}
