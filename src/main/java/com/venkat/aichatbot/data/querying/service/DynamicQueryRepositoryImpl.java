package com.venkat.aichatbot.data.querying.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


@Repository
public class DynamicQueryRepositoryImpl implements DynamicQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public List<Map<String, Object>> executeDynamicSql(String sql) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            SessionFactoryImplementor sessionFactory = entityManager
                    .getEntityManagerFactory()
                    .unwrap(SessionFactoryImplementor.class);

            Connection connection = sessionFactory
                    .getServiceRegistry()
                    .getService(ConnectionProvider.class)
                    .getConnection();

            try (Statement stmt = connection.createStatement()) {
                String lowerSql = sql.trim().toLowerCase();

                // If it's a SELECT query
                if (lowerSql.startsWith("select")) {
                    try (ResultSet rs = stmt.executeQuery(sql)) {
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
                    // For INSERT, UPDATE, COPY, CREATE, etc.
                    boolean hasResultSet = stmt.execute(sql);

                    Map<String, Object> map = new HashMap<>();
                    if (!hasResultSet) {
                        int updateCount = stmt.getUpdateCount();
                        map.put("info", "Statement executed. Update count: " + updateCount);
                    } else {
                        map.put("info", "Statement executed.");
                    }
                    resultList.add(map);
                }

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            Map<String, Object> result = new HashMap<>();

            if (message.contains("already exists")) {
                result.put("info", "Table already exists. Skipping creation.");
            } else {
                result.put("error", "SQL execution failed: " + e.getMessage());
            }

            resultList.add(result);
        }

        return resultList;
    }

    private void handleCopyCommand(String sql, Connection connection) throws Exception {
        // Extract table name and path
        Pattern pattern = Pattern.compile("COPY\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*FROM\\s+'([^']+)'", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid COPY command format");
        }

        String table = matcher.group(1);
        String columns = matcher.group(2);
        String path = matcher.group(3);

        Path filePath = Paths.get(path);
        List<Map<String, Object>> rows = fileUploadService.parseCsv(filePath);
        List<String> columnList = Arrays.stream(columns.split(","))
                .map(String::trim)
                .toList();

        try (PreparedStatement ps = connection.prepareStatement(buildInsertSql(table, columnList))) {
            for (Map<String, Object> row : rows) {
                for (int i = 0; i < columnList.size(); i++) {
                    ps.setObject(i + 1, row.get(columnList.get(i)));
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private String buildInsertSql(String table, List<String> columns) {
        String cols = String.join(", ", columns);
        String placeholders = columns.stream().map(col -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO " + table + " (" + cols + ") VALUES (" + placeholders + ")";
    }


}
