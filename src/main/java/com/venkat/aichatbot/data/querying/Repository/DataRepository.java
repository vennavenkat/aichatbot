package com.venkat.aichatbot.data.querying.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DataRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    public List<Object[]> fetchTableData(String tableName, int limit) {
        String queryStr = String.format("SELECT * FROM %s LIMIT %d", tableName, limit);
        return entityManager.createNativeQuery(queryStr).getResultList();
    }

    public List<String> fetchColumnNames(String tableName) {
        List<String> columnNames = new ArrayList<>();

        String columnQuery = "SELECT column_name FROM information_schema.columns " +
                "WHERE table_schema = 'public' AND table_name = :tableName";

        List<?> results = entityManager.createNativeQuery(columnQuery)
                .setParameter("tableName", tableName)
                .getResultList();

        for (Object col : results) {
            columnNames.add((String) col);
        }

        return columnNames;
    }
}
