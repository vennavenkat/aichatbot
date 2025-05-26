package com.venkat.aichatbot.data.querying.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;

@Repository
public class DynamicQueryRepositoryImpl implements DynamicQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;
//
//    @Override
//    public List<Map<String, Object>> executeDynamicSql(String sql) {
//        List<Map<String, Object>> resultList = new ArrayList<>();
//
//        try {
//            Query nativeQuery = entityManager.createNativeQuery(sql);
//            List<?> results = nativeQuery.getResultList();
//
//            // Use Hibernate unwrap to get JDBC metadata
//            if (results.isEmpty()) return resultList;
//
//            List<String> columnNames = new ArrayList<>();
//            if (results.get(0) instanceof Object[]) {
//                int columnCount = ((Object[]) results.get(0)).length;
//                for (int i = 0; i < columnCount; i++) {
//                    columnNames.add("col" + (i + 1)); // Generic fallback names
//                }
//            } else {
//                columnNames.add("col1");
//            }
//
//            for (Object row : results) {
//                Map<String, Object> map = new LinkedHashMap<>();
//
//                if (row instanceof Object[]) {
//                    Object[] rowArray = (Object[]) row;
//                    for (int i = 0; i < rowArray.length; i++) {
//                        map.put(columnNames.get(i), rowArray[i]);
//                    }
//                } else {
//                    map.put(columnNames.get(0), row); // Single column result
//                }
//
//                resultList.add(map);
//            }
//
//        } catch (Exception e) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", "SQL execution failed: " + e.getMessage());
//            resultList.add(error);
//        }
//
//        return resultList;
//    }
//
//
//    // Optionally parse column names from query or use a fallback
//    private List<String> getColumnNames(String sql) {
//        // In production, you'd parse the query or use metadata. Here’s a dummy fallback:
//        return Arrays.asList("col1", "col2", "col3"); // replace with logic if needed
//    }
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

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    row.put(columnName, rs.getObject(i));
                }
                resultList.add(row);
            }

        } finally {
            connection.close(); // always close!
        }

    } catch (Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "SQL execution failed: " + e.getMessage());
        resultList.add(error);
    }

    return resultList;
}



}
