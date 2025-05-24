package com.venkat.aichatbot.data.querying.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchemaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked") // Suppressing unchecked cast warnings for the result list
    public List<String> getAllTableNames() {
        return (List<String>) entityManager
                .createNativeQuery("SELECT tablename FROM pg_tables WHERE schemaname = 'public'")
                .getResultList();
    }

    @SuppressWarnings("unchecked") // Suppressing unchecked cast warnings for the result list
    public List<Object[]> getColumnsForTable(String tableName) {
        return (List<Object[]>) entityManager.createNativeQuery(
                        "SELECT column_name, data_type FROM information_schema.columns " +
                                "WHERE table_schema = 'public' AND table_name = :tableName"
                )
                .setParameter("tableName", tableName)
                .getResultList(); // returns List<Object[]> (each row = {column_name, data_type})
    }


}