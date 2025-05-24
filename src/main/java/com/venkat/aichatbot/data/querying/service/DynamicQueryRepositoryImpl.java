package com.venkat.aichatbot.data.querying.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DynamicQueryRepositoryImpl implements DynamicQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Map<String, Object>> executeDynamicSql(String sql) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            Query nativeQuery = entityManager.createNativeQuery(sql);
            List<?> results = nativeQuery.getResultList();

            if (results.isEmpty()) return resultList;

            List<String> columnNames = getColumnNames(sql);

            for (Object row : results) {
                Map<String, Object> map = new LinkedHashMap<>();

                if (row instanceof Object[]) {
                    Object[] rowArray = (Object[]) row;
                    for (int i = 0; i < rowArray.length; i++) {
                        map.put(columnNames.get(i), rowArray[i]);
                    }
                } else {
                    map.put(columnNames.get(0), row); // Single column result
                }

                resultList.add(map);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "SQL execution failed: " + e.getMessage());
            resultList.add(error);
        }

        return resultList;
    }

    // Optionally parse column names from query or use a fallback
    private List<String> getColumnNames(String sql) {
        // In production, you'd parse the query or use metadata. Here’s a dummy fallback:
        return Arrays.asList("col1", "col2", "col3"); // replace with logic if needed
    }
}
