package com.venkat.aichatbot.data.querying.service;

import com.venkat.aichatbot.data.querying.Repository.ChatLogRepository;
import com.venkat.aichatbot.data.querying.client.OpenAIClient;
import com.venkat.aichatbot.data.querying.dto.AskResponse;
import com.venkat.aichatbot.data.querying.entity.ChatLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

   /* public AskResponse processQuestion(String question) {
        // Fetch real-time schema details
        String schemaSummary = schemaService.getSchemaDetailsAsString(); // We'll implement this

        // Prompt
        String systemPrompt = "You are a data assistant. Given the following database schema:\n" +
                schemaSummary + "\n" +
                "Answer the user's question and also generate the SQL query inside <sql> </sql> tags.";

        // Append instruction to generate SQL
        String modifiedQuestion = question + ". Also convert this question into a SQL query using the schema above. Enclose SQL in <sql> </sql>.";

        // Ask GPT
        String gptResponse = openAIClient.askGPT(systemPrompt, modifiedQuestion);

        String sql = extractSqlFromResponse(gptResponse);
        String explanation = removeSqlFromResponse(gptResponse);

        ChatLog log = new ChatLog(null, question, sql, explanation, LocalDateTime.now());
        chatLogRepository.save(log);

        return new AskResponse(explanation.trim(), sql.trim());
    }*/


    public AskResponse processQuestion(String question) {
        // Step 1: Get schema to guide SQL generation
        String schemaSummary = schemaService.getSchemaDetailsAsString();
        String systemPrompt = "You are a data assistant. Given this database schema:\n" +
                schemaSummary + "\n" +
                "Answer the user's question and generate a SQL query wrapped in <sql> </sql>.";

        // Step 2: Append instruction
        String modifiedQuestion = question + ". Also generate the SQL query for this using the schema above. Enclose SQL in <sql> </sql>.";

        // Step 3: Call OpenAI
        String gptResponse = openAIClient.askGPT(systemPrompt, modifiedQuestion);

        // Step 4: Extract and clean
        String sql = extractSqlFromResponse(gptResponse);
        String explanation = removeSqlFromResponse(gptResponse);

        // Step 5: Execute SQL
        List<Map<String, Object>> resultSet = new ArrayList<>();
        if (!sql.isBlank()) {
            resultSet = executeDynamicSql(sql);
        }

        // Step 6: Log and return
        ChatLog log = new ChatLog(null, question, sql, explanation, LocalDateTime.now());
        chatLogRepository.save(log);

        AskResponse response = new AskResponse();
        response.setAnswer(explanation.trim());
        response.setGeneratedSql(sql.trim());
        response.setResultData(resultSet);

        return response;
    }
    public List<Map<String, Object>> executeDynamicSql(String sql) {
        return dynamicQueryRepository.executeDynamicSql(sql);
    }


    private String extractSqlFromResponse(String gptResponse) {
        if (gptResponse.contains("<sql>")) {
            int start = gptResponse.indexOf("<sql>") + 5;
            int end = gptResponse.indexOf("</sql>");
            if (start < end) return gptResponse.substring(start, end).trim();
        } else if (gptResponse.contains("```sql")) {
            int start = gptResponse.indexOf("```sql") + 6;
            int end = gptResponse.indexOf("```", start);
            if (start < end) return gptResponse.substring(start, end).trim();
        }
        return "";
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


    /*public List<Map<String, Object>> executeDynamicSql(String sql) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            // Get JDBC Connection from Hibernate's EntityManager
            SessionFactoryImplementor sessionFactory = entityManager
                    .getEntityManagerFactory()
                    .unwrap(SessionFactoryImplementor.class);

            Connection connection = sessionFactory
                    .getServiceRegistry()
                    .getService(ConnectionProvider.class)
                    .getConnection();

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                boolean hasResultSet = stmt.execute();

                if (hasResultSet) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        while (rs.next()) {
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(metaData.getColumnLabel(i), rs.getObject(i));
                            }
                            resultList.add(row);
                        }
                    }
                } else {
                    // For non-SELECT queries, log affected rows or throw if needed
                    int updateCount = stmt.getUpdateCount();
                    Map<String, Object> updateResult = new HashMap<>();
                    updateResult.put("message", "Query executed successfully. Rows affected: " + updateCount);
                    resultList.add(updateResult);
                }
            } finally {
                connection.close();
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "SQL execution failed: " + e.getMessage());
            resultList.add(error);
        }

        return resultList;
    }*/




//    private String normalizePostgreSQL(String sql) {
//        if (sql == null || sql.isBlank()) return sql;
//
//        return sql
//                // Convert MySQL CURDATE() to PostgreSQL CURRENT_DATE
//                .replaceAll("(?i)CURDATE\\(\\)", "CURRENT_DATE")
//                // Convert MySQL-style DATE() function
//                .replaceAll("(?i)DATE\\(([^)]+)\\)", "$1::date")
//                // Convert MySQL DATE_SUB to PostgreSQL equivalent
//                .replaceAll("(?i)DATE_SUB\\(CURRENT_DATE, INTERVAL (\\d+) MONTH\\)", "CURRENT_DATE - INTERVAL '$1 month'")
//                // Replace backticks with double quotes (PostgreSQL uses double quotes for identifiers)
//                .replace("`", "\"");
//    }




    /*private String extractSqlFromResponse(String gptResponse) {
        int start = gptResponse.indexOf("<sql>") + 5;
        int end = gptResponse.indexOf("</sql>");
        if (start == -1 || end == -1 || end <= start) return "";
        return gptResponse.substring(start, end);
    }*/

}
